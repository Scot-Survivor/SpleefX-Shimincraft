package net.spleefx.matchmaker;//package net.spleefx.matchmaker;
//
//import net.spleefx.arena.engine.TeamsArenaEngine;
//import net.spleefx.arena.player.MatchPlayer;
//import net.spleefx.arena.team.ArenaTeam;
//import net.spleefx.backend.Schedulers;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.LinkedList;
//import java.util.Objects;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Collectors;
//
//public final class EloTeamSelector {
//
//    public static CompletableFuture<ArenaTeam> pickFor(TeamsArenaEngine engine, @NotNull MatchPlayer player) {
//        CompletableFuture<ArenaTeam> teamFuture = new CompletableFuture<>();
//        Schedulers.POOL.submit(() -> {
//            EloResult selected = null;
//            LinkedList<EloResult> teams = engine.getArenaTeams()
//                    .stream()
//                    .filter(t -> !t.isFull())
//                    .map(EloResult::new) // this way we ensure we only call getElo() once, since EloResult caches it.
//                    .sorted(EloEvaluator::highest)
//                    .collect(Collectors.toCollection(LinkedList::new));
//
//            EloResult highest = teams.peek();
//            EloResult lowest = Objects.requireNonNull(teams.peekLast());
//
//
//        });
//        return teamFuture;
//    }
//
//    private static boolean isPositive(int i) {
//        return i > 0;
//    }
//
//    private static boolean isBetween(int value, int minimum, int maximum) {
//        return value >= minimum && value <= maximum;
//    }
//
//    private static int delta(EloResult first, EloResult second) {
//        return Math.abs(first.elo - second.elo);
//    }
//
//    private static int imaginarySquare(int v) {
//        return v * v * (isPositive(v) ? 1 : -1);
//    }
//
//    private static class EloResult implements EloComparable {
//
//        private final ArenaTeam team;
//        private final int elo;
//
//        public EloResult(ArenaTeam team) {
//            this.team = team;
//            elo = team.getElo(); // square to increase regression
//        }
//
//        @Override public int getElo() {
//            return elo;
//        }
//    }
//}