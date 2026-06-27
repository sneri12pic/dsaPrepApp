package com.stepandemianenko.dsaprep.presentation

import com.stepandemianenko.dsaprep.domain.model.ProblemDifficulty
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus

fun ProblemDifficulty.displayName(): String = when (this) {
    ProblemDifficulty.EASY -> "Easy"
    ProblemDifficulty.MEDIUM -> "Medium"
    ProblemDifficulty.HARD -> "Hard"
}

fun ProblemTopic.displayName(): String = when (this) {
    ProblemTopic.ARRAY -> "Arrays"
    ProblemTopic.HASHMAP -> "Hashmap"
    ProblemTopic.TWO_POINTERS -> "Two pointers"
    ProblemTopic.SLIDING_WINDOW -> "Sliding window"
    ProblemTopic.STACK -> "Stack"
    ProblemTopic.BINARY_SEARCH -> "Binary search"
    ProblemTopic.LINKED_LIST -> "Linked list"
    ProblemTopic.TREE -> "Tree"
    ProblemTopic.TRIE -> "Trie"
    ProblemTopic.GRAPH -> "Graph"
    ProblemTopic.ADVANCED_GRAPH -> "Advanced graph"
    ProblemTopic.HEAP -> "Heap"
    ProblemTopic.BACKTRACKING -> "Backtracking"
    ProblemTopic.DYNAMIC_PROGRAMMING -> "Dynamic programming"
    ProblemTopic.GREEDY -> "Greedy"
    ProblemTopic.INTERVALS -> "Intervals"
    ProblemTopic.BIT_MANIPULATION -> "Bit manipulation"
    ProblemTopic.MATH_GEOMETRY -> "Math & geometry"
    ProblemTopic.OTHER -> "Other"
}

fun SolvedStatus.displayName(): String = when (this) {
    SolvedStatus.SOLVED_MYSELF -> "Solved myself"
    SolvedStatus.SMALL_HINT -> "Small hint"
    SolvedStatus.MAJOR_HINT -> "Major hint"
    SolvedStatus.USED_SOLUTION -> "Used solution"
    SolvedStatus.COULD_NOT_SOLVE -> "Could not solve"
}

fun RewriteStatus.displayName(): String = when (this) {
    RewriteStatus.YES -> "Yes"
    RewriteStatus.PARTIALLY -> "Partially"
    RewriteStatus.NOT_YET -> "Not yet"
}
