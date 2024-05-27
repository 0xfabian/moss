import java.time.*;
import java.util.*;
import java.util.Map.Entry;

public class Scheduler {
    private static Scheduler instance;
    private final MedicalOffice mo;
    private final Map<String, Entry<String, Integer>> specializationLookup = new HashMap<>();

    Scheduler() {
        mo = MedicalOffice.getInstance();

        CSVConnection<Consultation> consultations = new CSVConnection<>("database/consultations.csv", Consultation::new);
        for (Consultation consultation : consultations.getList())
            specializationLookup.put(consultation.problem, new AbstractMap.SimpleEntry<>(consultation.specialization, consultation.duration));
    }

    public static Scheduler getInstance() {
        if (instance == null)
            instance = new Scheduler();

        return instance;
    }

    private List<Medic> getPotentialMedics(String problem) {
        List<Medic> potentialMedics = new ArrayList<>();
        String specialization = specializationLookup.get(problem).getKey();

        if (specialization != null) {
            for (Medic medic : mo.medics.getList())
                if (medic.getSpecialization().equals(specialization))
                    potentialMedics.add(medic);
        }

        return potentialMedics;
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private LocalDateTime findNextAvailableSlot(LocalDate startDate, Duration duration, LocalTime shiftStart, LocalTime shiftEnd, List<Appointment> appointments) {
        LocalDate currentDate = startDate;

        while (true) {
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                currentDate = currentDate.plusDays(1);
                continue;
            }

            LocalDateTime currentTime = currentDate.atTime(shiftStart);

            if (currentTime.isBefore(LocalDateTime.now()))
                currentTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1);

            while (currentTime.toLocalTime().isBefore(shiftEnd)) {
                boolean slotAvailable = true;

                for (Appointment appointment : appointments) {
                    if (isOverlapping(currentTime, currentTime.plus(duration), appointment.getDate(), appointment.getEnd())) {
                        currentTime = appointment.getEnd();
                        slotAvailable = false;
                        break;
                    }
                }

                if (slotAvailable) {
                    if (currentTime.plus(duration).toLocalTime().isBefore(shiftEnd) || currentTime.plus(duration).toLocalTime().equals(shiftEnd))
                        return currentTime;
                    else
                        break;
                }
            }

            currentDate = currentDate.plusDays(1);
        }
    }

    public void schedule(Appointment appointment) {
        List<Medic> potentialMedics = getPotentialMedics(appointment.getProblem());

        if (potentialMedics.isEmpty())
            throw new RuntimeException("No suitable medics was found");

        appointment.setDuration(Duration.ofHours(specializationLookup.get(appointment.getProblem()).getValue()));
        List<LocalDateTime> potentialDates = new ArrayList<>();

        for (Medic medic : potentialMedics) {
            LocalDate startDate = LocalDate.now();
            Duration duration = appointment.getDuration();
            List<Appointment> futureAppointments = medic.getFutureAppointments();
            LocalTime startTime = LocalTime.of(medic.getShiftStart(), 0);
            LocalTime endTime = LocalTime.of(medic.getShiftEnd(), 0);
            potentialDates.add(findNextAvailableSlot(startDate, duration, startTime, endTime, futureAppointments));
        }

        Medic medic = potentialMedics.get(0);
        LocalDateTime date = potentialDates.get(0);

        for (int i = 1; i < potentialDates.size(); i++) {
            if (potentialDates.get(i).isBefore(date)) {
                date = potentialDates.get(i);
                medic = potentialMedics.get(i);
            }
        }

        appointment.setMedicId(medic.getId());
        appointment.setDate(date);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
    }
}
