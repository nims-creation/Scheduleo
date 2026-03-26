package com.saas.Schedulo.service.impl.ai;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiScheduleServiceImpl implements AiScheduleService {
    private final ChatClient chatClient;
    private final TimetableRepository timetableRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ConflictDetectionService conflictDetectionService;
    private final ObjectMapper objectMapper;
    @Override
    public List<CreateScheduleEntryRequest> suggestOptimalSchedule(
            UUID organizationId,
            UUID timetableId,
            LocalDate startDate,
            LocalDate endDate,
            SchedulePreferences preferences) {
        Timetable timetable = timetableRepository.findByIdWithDetails(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", timetableId));
        List<ScheduleEntry> existingEntries = scheduleEntryRepository.findByTimetableId(timetableId);
        String prompt = buildScheduleOptimizationPrompt(timetable, existingEntries, startDate, endDate, preferences);
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        try {
            return objectMapper.readValue(response, new TypeReference<List<CreateScheduleEntryRequest>>() {});
        } catch (Exception e) {
            log.error("Failed to parse AI schedule suggestions: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    @Override
    public List<ScheduleEntryResponse> detectAndResolveConflicts(
            UUID timetableId,
            List<CreateScheduleEntryRequest> entries) {
        // Use AI to suggest conflict resolutions
        String conflictPrompt = buildConflictResolutionPrompt(entries);
        String response = chatClient.prompt()
                .user(conflictPrompt)
                .call()
                .content();
        // Parse and return resolved entries
        try {
            List<CreateScheduleEntryRequest> resolved = objectMapper.readValue(
                    response, new TypeReference<List<CreateScheduleEntryRequest>>() {}
            );
            // Convert to responses (simplified)
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to parse conflict resolution: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    @Override
    public String generateScheduleSummary(UUID timetableId) {
        Timetable timetable = timetableRepository.findByIdWithDetails(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", timetableId));
        List<ScheduleEntry> entries = scheduleEntryRepository.findByTimetableId(timetableId);
        String prompt = String.format("""
            Generate a human-readable summary of the following timetable:
            
            Timetable: %s
            Type: %s
            Effective: %s to %s
            Total Entries: %d
            
            Entries by day:
            %s
            
            Provide a brief, informative summary including:
            1. Overall schedule structure
            2. Busiest days
            3. Any notable patterns
            """,
                timetable.getName(),
                timetable.getTimetableType(),
                timetable.getEffectiveFrom(),
                timetable.getEffectiveTo(),
                entries.size(),
                formatEntriesForAi(entries)
        );
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
    @Override
    public ScheduleAnalysis analyzeScheduleEfficiency(UUID timetableId) {
        Timetable timetable = timetableRepository.findByIdWithDetails(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", "id", timetableId));
        List<ScheduleEntry> entries = scheduleEntryRepository.findByTimetableId(timetableId);
        // Calculate basic metrics
        double utilizationRate = calculateUtilizationRate(timetable, entries);
        int conflictCount = countConflicts(entries);
        Map<String, Double> resourceUtilization = calculateResourceUtilization(entries);
        // Get AI recommendations
        String analysisPrompt = buildAnalysisPrompt(timetable, entries, utilizationRate, conflictCount);
        String aiRecommendations = chatClient.prompt()
                .user(analysisPrompt)
                .call()
                .content();
        List<String> recommendations = Arrays.asList(aiRecommendations.split("\n"));
        return new ScheduleAnalysis(
                utilizationRate,
                conflictCount,
                recommendations,
                resourceUtilization
        );
    }
    private String buildScheduleOptimizationPrompt(
            Timetable timetable,
            List<ScheduleEntry> existingEntries,
            LocalDate startDate,
            LocalDate endDate,
            SchedulePreferences preferences) {
        return String.format("""
            You are a schedule optimization AI. Generate optimal schedule entries for the following timetable.
            
            Timetable Details:
            - Name: %s
            - Type: %s
            - Organization Type: %s
            
            Existing Entries: %d
            
            Date Range: %s to %s
            
            Preferences:
            - Max entries per day: %d
            - Min break between entries: %d minutes
            - Preferred time slots: %s
            - Days to avoid: %s
            - Balance workload: %s
            
            Available Time Slots:
            %s
            
            Return a JSON array of schedule entry objects with fields:
            - title (string)
            - dayOfWeek (string: MONDAY, TUESDAY, etc.)
            - scheduleDate (string: YYYY-MM-DD)
            - startDatetime (string: ISO datetime)
            - endDatetime (string: ISO datetime)
            - entryType (string: CLASS, MEETING, etc.)
            
            Optimize for minimal conflicts and balanced distribution.
            """,
                timetable.getName(),
                timetable.getTimetableType(),
                timetable.getOrganization().getOrganizationType(),
                existingEntries.size(),
                startDate,
                endDate,
                preferences.maxEntriesPerDay(),
                preferences.minBreakBetweenEntries(),
                preferences.preferredTimeSlots(),
                preferences.avoidDays(),
                preferences.balanceWorkload(),
                formatTimeSlotsForAi(timetable)
        );
    }
    private String buildConflictResolutionPrompt(List<CreateScheduleEntryRequest> entries) {
        return "Analyze and resolve scheduling conflicts in the following entries: " + entries.toString();
    }
    private String buildAnalysisPrompt(
            Timetable timetable,
            List<ScheduleEntry> entries,
            double utilizationRate,
            int conflictCount) {
        return String.format("""
            Analyze the following timetable and provide recommendations for improvement:
            
            Timetable: %s
            Utilization Rate: %.2f%%
            Conflict Count: %d
            Total Entries: %d
            
            Provide 3-5 specific recommendations to improve schedule efficiency.
            Format each recommendation on a new line.
            """,
                timetable.getName(),
                utilizationRate * 100,
                conflictCount,
                entries.size()
        );
    }
    private String formatEntriesForAi(List<ScheduleEntry> entries) {
        // Format entries for AI consumption
        StringBuilder sb = new StringBuilder();
        entries.stream()
                .collect(java.util.stream.Collectors.groupingBy(ScheduleEntry::getDayOfWeek))
                .forEach((day, dayEntries) -> {
                    sb.append(day).append(": ").append(dayEntries.size()).append(" entries\n");
                });
        return sb.toString();
    }
    private String formatTimeSlotsForAi(Timetable timetable) {
        return timetable.getTimeSlots().stream()
                .map(slot -> String.format("%s: %s - %s",
                        slot.getSlotName(), slot.getStartTime(), slot.getEndTime()))
                .collect(java.util.stream.Collectors.joining("\n"));
    }
    private double calculateUtilizationRate(Timetable timetable, List<ScheduleEntry> entries) {
        // Simplified calculation
        int totalSlots = timetable.getTimeSlots().size() * 5; // Assuming 5 working days
        return totalSlots > 0 ? (double) entries.size() / totalSlots : 0.0;
    }
    private int countConflicts(List<ScheduleEntry> entries) {
        // Simplified conflict count
        return 0;
    }
    private Map<String, Double> calculateResourceUtilization(List<ScheduleEntry> entries) {
        Map<String, Double> utilization = new HashMap<>();
        entries.stream()
                .filter(e -> e.getResource() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getResource().getName(),
                        java.util.stream.Collectors.counting()
                ))
                .forEach((name, count) -> utilization.put(name, count.doubleValue()));
        return utilization;
    }
}
