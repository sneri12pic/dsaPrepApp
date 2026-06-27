package com.stepandemianenko.dsaprep.domain.model

data class KnownProblem(
    val title: String,
    val link: String,
    val difficulty: ProblemDifficulty,
    val topic: ProblemTopic,
    val pattern: String
) {
    val leetcodeUrl: String
        get() = "https://leetcode.com/problems/$link"
}
