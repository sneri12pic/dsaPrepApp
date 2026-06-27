package com.stepandemianenko.dsaprep.domain.logic

import com.stepandemianenko.dsaprep.domain.model.ProblemSession
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus

object RestAdviceCalculator {
    fun adviceFor(session: ProblemSession): String {
        if (session.durationSeconds >= 60 * 60) {
            return "You spent a lot of energy. Do light review only. Avoid forcing another hard problem now."
        }

        return when (session.solvedStatus) {
            SolvedStatus.USED_SOLUTION -> {
                "Do not start a new problem immediately. Take 10 minutes away from the screen, then rewrite the solution from memory."
            }
            SolvedStatus.COULD_NOT_SOLVE -> {
                "Stop the timer, review the core pattern, write one takeaway, and take a proper break. This still counts as learning."
            }
            SolvedStatus.SMALL_HINT,
            SolvedStatus.MAJOR_HINT -> {
                "Take a 15-minute break. After that, redo the key part without looking. Do not rush into a new problem."
            }
            SolvedStatus.SOLVED_MYSELF -> {
                if (session.confidence <= 3) {
                    "Take a 10-15 minute break, then rewrite the solution from memory before starting a new problem."
                } else if (session.durationSeconds <= session.goalMinutes * 60L && session.confidence >= 4) {
                    "Good session. Take a 10-15 minute break, then do one similar problem or a slightly harder variation."
                } else {
                    "Take a 10-15 minute break, then review the approach and decide whether to repeat the pattern or move on."
                }
            }
        }
    }
}
