package com.stepandemianenko.dsaprep.data

import com.stepandemianenko.dsaprep.domain.logic.ProgressCalculator
import com.stepandemianenko.dsaprep.domain.logic.SessionProgressSummary
import com.stepandemianenko.dsaprep.domain.model.ProblemDifficulty
import com.stepandemianenko.dsaprep.domain.model.ProblemSession
import com.stepandemianenko.dsaprep.domain.model.ProblemTopic
import com.stepandemianenko.dsaprep.domain.model.RewriteStatus
import com.stepandemianenko.dsaprep.domain.model.SolvedStatus
import com.stepandemianenko.dsaprep.domain.repository.ProblemSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudyRepository(
    private val dao: StudyDao
) : ProblemSessionRepository {
    override suspend fun insertSession(
        problemTitle: String,
        problemLink: String?,
        difficulty: ProblemDifficulty,
        topic: ProblemTopic,
        startedAtEpochMillis: Long,
        finishedAtEpochMillis: Long,
        durationSeconds: Long,
        goalMinutes: Int,
        solvedStatus: SolvedStatus,
        timeComplexity: String?,
        spaceComplexity: String?,
        mainApproach: String?,
        mistakeOrBlocker: String?,
        confidence: Int,
        dateIso: String
    ): Long {
        return dao.insertProblemSession(
            ProblemSessionEntity(
                problemTitle = problemTitle.trim(),
                problemLink = problemLink?.trim()?.takeIf { it.isNotBlank() },
                difficulty = difficulty.name,
                topic = topic.name,
                startedAtEpochMillis = startedAtEpochMillis,
                finishedAtEpochMillis = finishedAtEpochMillis,
                durationSeconds = durationSeconds,
                goalMinutes = goalMinutes,
                solvedStatus = solvedStatus.name,
                timeComplexity = timeComplexity.cleanOptional(),
                spaceComplexity = spaceComplexity.cleanOptional(),
                mainApproach = mainApproach.cleanOptional(),
                mistakeOrBlocker = mistakeOrBlocker.cleanOptional(),
                confidence = confidence.coerceIn(1, 5),
                reviewPattern = null,
                reviewKeyInsight = null,
                reviewMistake = null,
                reviewRewriteStatus = null,
                reviewFinalTakeaway = null,
                dateIso = dateIso
            )
        )
    }

    override suspend fun updateSessionReview(
        sessionId: Long,
        reviewPattern: String?,
        reviewKeyInsight: String?,
        reviewMistake: String?,
        reviewRewriteStatus: RewriteStatus?,
        reviewFinalTakeaway: String?
    ) {
        dao.updateProblemSessionReview(
            sessionId = sessionId,
            reviewPattern = reviewPattern.cleanOptional(),
            reviewKeyInsight = reviewKeyInsight.cleanOptional(),
            reviewMistake = reviewMistake.cleanOptional(),
            reviewRewriteStatus = reviewRewriteStatus?.name,
            reviewFinalTakeaway = reviewFinalTakeaway.cleanOptional()
        )
    }

    suspend fun getSessionById(sessionId: Long): ProblemSession? {
        return dao.getProblemSessionById(sessionId)?.toProblemSession()
    }

    override fun getSessionsForDate(dateIso: String): Flow<List<ProblemSession>> {
        return dao.getProblemSessionsForDate(dateIso).map { sessions ->
            sessions.map { it.toProblemSession() }
        }
    }

    override fun getRecentSessions(limit: Int): Flow<List<ProblemSession>> {
        return dao.getRecentProblemSessions(limit).map { sessions ->
            sessions.map { it.toProblemSession() }
        }
    }

    override fun getAllSessions(): Flow<List<ProblemSession>> {
        return dao.getAllProblemSessions().map { sessions ->
            sessions.map { it.toProblemSession() }
        }
    }

    fun getProgressSummary(todayIso: String): Flow<SessionProgressSummary> {
        return getAllSessions().map { sessions ->
            ProgressCalculator.calculate(sessions, todayIso)
        }
    }

    fun getRoadmap(): List<RoadmapWeek> = listOf(
        RoadmapWeek(
            title = "Week A",
            subtitle = "Core patterns",
            items = listOf(
                RoadmapItem(ProblemTopic.ARRAY, "Arrays & Hashing", "3 problems", "Track brute force vs hash-based improvements."),
                RoadmapItem(ProblemTopic.TWO_POINTERS, "Two Pointers", "2 problems", "Say the invariant before coding."),
                RoadmapItem(ProblemTopic.SLIDING_WINDOW, "Sliding Window", "2 problems", "Write when the window expands and shrinks."),
                RoadmapItem(ProblemTopic.STACK, "Stack", "2 problems", "Look for nearest greater/smaller or undo-like state."),
                RoadmapItem(ProblemTopic.BINARY_SEARCH, "Binary Search", "2 problems", "Define the search space and loop condition first."),
                RoadmapItem(ProblemTopic.OTHER, "Review / redo", "2 repeats", "Redo missed problems from memory."),
                RoadmapItem(ProblemTopic.OTHER, "Rest + light recap", "20 minutes", "Review notes without forcing a new hard problem.")
            )
        ),
        RoadmapWeek(
            title = "Week B",
            subtitle = "Structures",
            items = listOf(
                RoadmapItem(ProblemTopic.LINKED_LIST, "Linked List", "2 problems", "Draw pointer movement before writing code."),
                RoadmapItem(ProblemTopic.TREE, "Trees DFS", "2 problems", "Name preorder/inorder/postorder intent."),
                RoadmapItem(ProblemTopic.TREE, "Trees BFS", "2 problems", "Track queue state and level boundaries."),
                RoadmapItem(ProblemTopic.HEAP, "Heap / Priority Queue", "2 problems", "State why ordering by min/max helps."),
                RoadmapItem(ProblemTopic.BACKTRACKING, "Backtracking", "2 problems", "Write choose/explore/unchoose clearly."),
                RoadmapItem(ProblemTopic.GRAPH, "Graph Basics", "2 problems", "Mark visited rules before coding traversal."),
                RoadmapItem(ProblemTopic.OTHER, "Review / redo", "2 repeats", "Pick the weakest topic from Progress.")
            )
        )
    )

    /**
     * Blind 75 grouped by category. `done = true` marks problems already solved on
     * NeetCode (from the user's submission repo); the rest start unchecked.
     */
    fun getBlind75(): List<RoadmapWeek> {
        fun p(topic: ProblemTopic, title: String, difficulty: String, done: Boolean = false) =
            RoadmapItem(topic, title, difficulty, "", done)

        fun group(title: String, items: List<RoadmapItem>) =
            RoadmapWeek(title, "${items.size} problems", items)

        return listOf(
            group("Array", listOf(
                p(ProblemTopic.ARRAY, "Two Sum", "Easy", done = true),
                p(ProblemTopic.ARRAY, "Best Time to Buy and Sell Stock", "Easy", done = true),
                p(ProblemTopic.ARRAY, "Contains Duplicate", "Easy", done = true),
                p(ProblemTopic.ARRAY, "Product of Array Except Self", "Medium", done = true),
                p(ProblemTopic.ARRAY, "Maximum Subarray", "Medium"),
                p(ProblemTopic.ARRAY, "Maximum Product Subarray", "Medium"),
                p(ProblemTopic.ARRAY, "Find Minimum in Rotated Sorted Array", "Medium", done = true),
                p(ProblemTopic.ARRAY, "Search in Rotated Sorted Array", "Medium", done = true),
                p(ProblemTopic.ARRAY, "3Sum", "Medium", done = true),
                p(ProblemTopic.ARRAY, "Container With Most Water", "Medium", done = true),
                p(ProblemTopic.ARRAY, "Longest Consecutive Sequence", "Medium", done = true)
            )),
            group("Binary", listOf(
                p(ProblemTopic.OTHER, "Sum of Two Integers", "Medium"),
                p(ProblemTopic.OTHER, "Number of 1 Bits", "Easy"),
                p(ProblemTopic.OTHER, "Counting Bits", "Easy"),
                p(ProblemTopic.OTHER, "Missing Number", "Easy"),
                p(ProblemTopic.OTHER, "Reverse Bits", "Easy")
            )),
            group("Dynamic Programming", listOf(
                p(ProblemTopic.OTHER, "Climbing Stairs", "Easy"),
                p(ProblemTopic.OTHER, "Coin Change", "Medium"),
                p(ProblemTopic.OTHER, "Longest Increasing Subsequence", "Medium"),
                p(ProblemTopic.OTHER, "Longest Common Subsequence", "Medium"),
                p(ProblemTopic.OTHER, "Word Break", "Medium"),
                p(ProblemTopic.BACKTRACKING, "Combination Sum", "Medium"),
                p(ProblemTopic.OTHER, "House Robber", "Medium"),
                p(ProblemTopic.OTHER, "House Robber II", "Medium"),
                p(ProblemTopic.OTHER, "Decode Ways", "Medium"),
                p(ProblemTopic.OTHER, "Unique Paths", "Medium"),
                p(ProblemTopic.OTHER, "Jump Game", "Medium")
            )),
            group("Graph", listOf(
                p(ProblemTopic.GRAPH, "Clone Graph", "Medium"),
                p(ProblemTopic.GRAPH, "Course Schedule", "Medium"),
                p(ProblemTopic.GRAPH, "Pacific Atlantic Water Flow", "Medium"),
                p(ProblemTopic.GRAPH, "Number of Islands", "Medium"),
                p(ProblemTopic.GRAPH, "Graph Valid Tree", "Medium"),
                p(ProblemTopic.GRAPH, "Number of Connected Components in an Undirected Graph", "Medium"),
                p(ProblemTopic.GRAPH, "Alien Dictionary", "Hard")
            )),
            group("Interval", listOf(
                p(ProblemTopic.OTHER, "Insert Interval", "Medium"),
                p(ProblemTopic.OTHER, "Merge Intervals", "Medium"),
                p(ProblemTopic.OTHER, "Non-overlapping Intervals", "Medium"),
                p(ProblemTopic.OTHER, "Meeting Rooms", "Easy"),
                p(ProblemTopic.OTHER, "Meeting Rooms II", "Medium")
            )),
            group("Linked List", listOf(
                p(ProblemTopic.LINKED_LIST, "Reverse Linked List", "Easy", done = true),
                p(ProblemTopic.LINKED_LIST, "Linked List Cycle", "Easy", done = true),
                p(ProblemTopic.LINKED_LIST, "Merge Two Sorted Lists", "Easy", done = true),
                p(ProblemTopic.LINKED_LIST, "Merge K Sorted Lists", "Hard"),
                p(ProblemTopic.LINKED_LIST, "Remove Nth Node From End of List", "Medium", done = true),
                p(ProblemTopic.LINKED_LIST, "Reorder List", "Medium", done = true)
            )),
            group("Matrix", listOf(
                p(ProblemTopic.OTHER, "Set Matrix Zeroes", "Medium"),
                p(ProblemTopic.OTHER, "Spiral Matrix", "Medium"),
                p(ProblemTopic.OTHER, "Rotate Image", "Medium"),
                p(ProblemTopic.OTHER, "Word Search", "Medium")
            )),
            group("String", listOf(
                p(ProblemTopic.SLIDING_WINDOW, "Longest Substring Without Repeating Characters", "Medium", done = true),
                p(ProblemTopic.SLIDING_WINDOW, "Longest Repeating Character Replacement", "Medium", done = true),
                p(ProblemTopic.SLIDING_WINDOW, "Minimum Window Substring", "Hard"),
                p(ProblemTopic.ARRAY, "Valid Anagram", "Easy", done = true),
                p(ProblemTopic.ARRAY, "Group Anagrams", "Medium", done = true),
                p(ProblemTopic.STACK, "Valid Parentheses", "Easy", done = true),
                p(ProblemTopic.TWO_POINTERS, "Valid Palindrome", "Easy", done = true),
                p(ProblemTopic.TWO_POINTERS, "Longest Palindromic Substring", "Medium"),
                p(ProblemTopic.TWO_POINTERS, "Palindromic Substrings", "Medium"),
                p(ProblemTopic.ARRAY, "Encode and Decode Strings", "Medium")
            )),
            group("Tree", listOf(
                p(ProblemTopic.TREE, "Maximum Depth of Binary Tree", "Easy", done = true),
                p(ProblemTopic.TREE, "Same Tree", "Easy", done = true),
                p(ProblemTopic.TREE, "Invert Binary Tree", "Easy", done = true),
                p(ProblemTopic.TREE, "Binary Tree Maximum Path Sum", "Hard"),
                p(ProblemTopic.TREE, "Binary Tree Level Order Traversal", "Medium"),
                p(ProblemTopic.TREE, "Serialize and Deserialize Binary Tree", "Hard"),
                p(ProblemTopic.TREE, "Subtree of Another Tree", "Easy", done = true),
                p(ProblemTopic.TREE, "Construct Binary Tree from Preorder and Inorder Traversal", "Medium"),
                p(ProblemTopic.TREE, "Validate Binary Search Tree", "Medium"),
                p(ProblemTopic.TREE, "Kth Smallest Element in a BST", "Medium"),
                p(ProblemTopic.TREE, "Lowest Common Ancestor of a BST", "Easy", done = true),
                p(ProblemTopic.TREE, "Implement Trie (Prefix Tree)", "Medium"),
                p(ProblemTopic.TREE, "Add and Search Word", "Medium"),
                p(ProblemTopic.TREE, "Word Search II", "Hard")
            )),
            group("Heap", listOf(
                p(ProblemTopic.HEAP, "Top K Frequent Elements", "Medium", done = true),
                p(ProblemTopic.HEAP, "Find Median from Data Stream", "Hard")
            ))
        )
    }
}

data class RoadmapWeek(
    val title: String,
    val subtitle: String,
    val items: List<RoadmapItem>
)

data class RoadmapItem(
    val topic: ProblemTopic,
    val title: String,
    val target: String,
    val advice: String,
    /** Pre-completed out of the box (e.g. already solved on NeetCode). */
    val done: Boolean = false
)

private fun String?.cleanOptional(): String? = this?.trim()?.takeIf { it.isNotBlank() }
