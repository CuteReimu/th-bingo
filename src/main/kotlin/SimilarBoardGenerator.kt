package org.tfcc.bingo

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.random.asKotlinRandom

object SimilarBoardGenerator {
    private const val SIZE = 5
    private const val MATRIX_AREA = SIZE * SIZE
    private val V_MAP = intArrayOf(0, 1, 1, 2, 4, 5, 4, 5) // V(x) = V_MAP[x]

    // 主入口函数
    fun findMatrixB(matrixA: IntArray, targetDiff: Int): IntArray {
        if (targetDiff <= 1) {
            return matrixA.copyOf()
        }

        // 1. 预处理：统计A中1,2,3的数量
        val requiredCounts = mutableMapOf<Int, Int>()
        matrixA.filter { it in 1..3 || it in 6..7 }.forEach { num ->
            requiredCounts[num] = requiredCounts.getOrDefault(num, 0) + 1
        }

        // 2. 生成并随机化所有可能的骨架
        val skeletons = generateAndShuffleSkeletons()

        // 3. 初始化“最佳解”追踪变量
        var bestSolutionSoFar: IntArray? = null
        var minAbsDifference = Int.MAX_VALUE

        // 4. 遍历骨架，寻找解
        for ((index, skeleton) in skeletons.withIndex()) {
            // solveForSkeleton现在返回它能为该骨架找到的最佳解及其差异度
            val (candidateMatrix, actualDiff) = solveForSkeleton(
                skeleton,
                matrixA,
                targetDiff,
                requiredCounts
            )

            val currentAbsDiff = abs(actualDiff - targetDiff)

            // 检查是否是“完美解”，如果是则提前退出
            if (currentAbsDiff <= 1) {
                return candidateMatrix
            }

            // 否则，检查是否是“迄今为止最好的解”
            if (currentAbsDiff < minAbsDifference) {
                minAbsDifference = currentAbsDiff
                bestSolutionSoFar = candidateMatrix
            }
        }

        // 5. 如果循环结束仍未找到完美解，返回记录的最佳解
        return bestSolutionSoFar!! // 因为至少有一个骨架，所以不会为null
    }

    /**
     * 核心求解函数：为给定骨架寻找最接近目标的填充方案
     * @return Pair<IntArray, Int> - 返回找到的最佳矩阵及其对应的实际差异度
     */
    private fun solveForSkeleton(
        skeleton: Map<Int, Int>,
        matrixA: IntArray,
        targetDiff: Int,
        requiredCounts: Map<Int, Int>
    ): Pair<IntArray, Int> {
        val skeletonDiff = skeleton.entries.sumOf { (idx, value) ->
            abs(V_MAP[matrixA[idx]] - V_MAP[value])
        }
        val fleshIndices = (0 until MATRIX_AREA).filter { it !in skeleton.keys }.toList()

        val (initialFlesh, fleshMinDiff) = buildMinDiffFlesh(fleshIndices, requiredCounts, matrixA)

        var currentTotalDiff = skeletonDiff + fleshMinDiff
        var currentFlesh = initialFlesh.toMutableMap()

        // 追踪此骨架下的最佳状态
        var bestFleshForThisSkeleton = currentFlesh
        var bestTotalDiffForThisSkeleton = currentTotalDiff
        var minGapForThisSkeleton = abs(currentTotalDiff - targetDiff)

        // 剪枝：如果最小差异度已经过大，直接返回这个最小差异度的解
        if (currentTotalDiff > targetDiff) {
            return Pair(buildFullMatrix(skeleton, bestFleshForThisSkeleton), bestTotalDiffForThisSkeleton)
        }

        while (true) {
            var bestSwap: Pair<Int, Int>? = null
            var bestSwapNewTotalDiff = -1
            var minTargetGapAfterSwap = Int.MAX_VALUE

            for (i in fleshIndices.indices) {
                for (j in (i + 1) until fleshIndices.size) {
                    val idx1 = fleshIndices[i]
                    val idx2 = fleshIndices[j]
                    val val1 = currentFlesh[idx1]!!
                    val val2 = currentFlesh[idx2]!!

                    if (val1 == val2) continue

                    val diffBefore = abs(V_MAP[matrixA[idx1]] - V_MAP[val1]) + abs(V_MAP[matrixA[idx2]] - V_MAP[val2])
                    val diffAfter = abs(V_MAP[matrixA[idx1]] - V_MAP[val2]) + abs(V_MAP[matrixA[idx2]] - V_MAP[val1])
                    val delta = diffAfter - diffBefore

                    if (delta > 0) {
                        val newTotalDiff = currentTotalDiff + delta
                        val newGap = abs(newTotalDiff - targetDiff)
                        if (newGap < minTargetGapAfterSwap) {
                            minTargetGapAfterSwap = newGap
                            bestSwap = Pair(idx1, idx2)
                            bestSwapNewTotalDiff = newTotalDiff
                        }
                    }
                }
            }

            if (bestSwap != null && minTargetGapAfterSwap < minGapForThisSkeleton) {
                val (idx1, idx2) = bestSwap
                val temp = currentFlesh[idx1]!!
                currentFlesh[idx1] = currentFlesh[idx2]!!
                currentFlesh[idx2] = temp
                currentTotalDiff = bestSwapNewTotalDiff

                // 更新此骨架下的最佳状态
                minGapForThisSkeleton = minTargetGapAfterSwap
                bestFleshForThisSkeleton = currentFlesh.toMutableMap() // 保存副本
                bestTotalDiffForThisSkeleton = currentTotalDiff

                print("->")
            } else {
                break // 退出循环
            }
        }

        return Pair(buildFullMatrix(skeleton, bestFleshForThisSkeleton), bestTotalDiffForThisSkeleton)
    }

    private fun buildMinDiffFlesh(
        fleshIndices: List<Int>,
        requiredCounts: Map<Int, Int>,
        matrixA: IntArray
    ): Pair<Map<Int, Int>, Int> {
        val counts = requiredCounts.toMutableMap()
        val fleshAssignment = mutableMapOf<Int, Int>()
        val candidates = mutableListOf<Triple<Int, Int, Int>>()
        for (idx in fleshIndices) {
            for (value in 1..3) {
                val cost = abs(V_MAP[matrixA[idx]] - V_MAP[value])
                candidates.add(Triple(idx, value, cost))
            }
            for (value in 6..7) {
                val cost = abs(V_MAP[matrixA[idx]] - V_MAP[value])
                candidates.add(Triple(idx, value, cost))
            }
        }
        candidates.sortBy { it.third }
        val filledIndices = mutableSetOf<Int>()
        for ((idx, value, _) in candidates) {
            if (idx !in filledIndices && (counts[value] ?: 0) > 0) {
                fleshAssignment[idx] = value
                counts[value] = counts.getValue(value) - 1
                filledIndices.add(idx)
            }
        }
        val totalDiff = fleshAssignment.entries.sumOf { (idx, value) ->
            abs(V_MAP[matrixA[idx]] - V_MAP[value])
        }
        return Pair(fleshAssignment, totalDiff)
    }

    private fun generateAndShuffleSkeletons(): List<Map<Int, Int>> {
        val skeletons = mutableListOf<Map<Int, Int>>()
        val otherRowsCols = listOf(0, 1, 3, 4)
        val permutations = mutableListOf<List<Int>>()
        fun generatePermutations(list: List<Int>, current: List<Int> = emptyList()) {
            if (list.isEmpty()) {
                permutations.add(current)
                return
            }
            for (i in list.indices) {
                val newList = list.toMutableList()
                val elem = newList.removeAt(i)
                generatePermutations(newList, current + elem)
            }
        }
        generatePermutations(otherRowsCols)
        for (p in permutations) {
            val positions = mutableListOf<Int>()
            positions.add(2 * SIZE + 2)
            for (i in otherRowsCols.indices) {
                positions.add(otherRowsCols[i] * SIZE + p[i])
            }
            for (i in positions.indices) {
                val skeleton = mutableMapOf<Int, Int>()
                for (j in positions.indices) {
                    val pos = positions[j]
                    skeleton[pos] = if (i == j) 5 else 4
                }
                skeletons.add(skeleton)
            }
        }
        return skeletons.shuffled(ThreadLocalRandom.current().asKotlinRandom())
    }

    private fun buildFullMatrix(skeleton: Map<Int, Int>, flesh: Map<Int, Int>): IntArray {
        val matrixB = IntArray(MATRIX_AREA)
        for ((idx, value) in skeleton) matrixB[idx] = value
        for ((idx, value) in flesh) matrixB[idx] = value
        return matrixB
    }
}

/*

fun main() {
    // 1. 创建一个符合规则的矩阵A
    val matrixA = intArrayOf(
        5, 7, 3, 6, 6,
        3, 4, 7, 3, 3,
        3, 3, 4, 3, 3,
        3, 3, 1, 4, 1,
        1, 1, 1, 3, 4
    )
    // 统计A中数字数量: 5:1, 4:4, 3:4, 2:4, 1:12

    println("原始矩阵 A:")
    printMatrix(matrixA)

    // 测试一个较远的目标，以便观察“返回最接近解”的行为
    val targetDiff = 66

    println("\n目标差异度: $targetDiff")

    val startTime = System.currentTimeMillis()
    val matrixB = SimilarBoardGenerator.findMatrixB(matrixA, targetDiff)
    val endTime = System.currentTimeMillis()

    println("算法执行完毕，耗时: ${endTime - startTime} ms")
    println("\n找到的矩阵 B:")
    printMatrix(matrixB)

    val actualDiff = calculateDifference(matrixA, matrixB)
    println("\n实际差异度: $actualDiff")
    println("与目标差异度的绝对差: ${abs(actualDiff - targetDiff)}")

    // 验证数字总数 (兼容性修复)
    val countsA = mutableMapOf<Int, Int>()
    matrixA.forEach { countsA[it] = countsA.getOrDefault(it, 0) + 1 }
    val countsB = mutableMapOf<Int, Int>()
    matrixB.forEach { countsB[it] = countsB.getOrDefault(it, 0) + 1 }

    println("A矩阵数字统计: $countsA")
    println("B矩阵数字统计: $countsB")
    println("数字总数是否相同: ${countsA == countsB}")

    println(">3差异：${calculateDifference2(matrixA, matrixB)}")
    println("元素差异：${calculateDifference3(matrixA, matrixB)}")
}

// 辅助打印和计算函数
private val V_MAP_TEST = intArrayOf(0, 1, 1, 2, 4, 5, 4, 5)

fun printMatrix(matrix: IntArray) {
    for (i in 0 until 5) {
        for (j in 0 until 5) {
            print("${matrix[i * 5 + j]} ")
        }
        println()
    }
}

fun calculateDifference(matrixA: IntArray, matrixB: IntArray): Int {
    return matrixA.indices.sumOf { i ->
        abs(V_MAP_TEST[matrixA[i]] - V_MAP_TEST[matrixB[i]])
    }
}

fun calculateDifference2(matrixA: IntArray, matrixB: IntArray): Int {
    return matrixA.indices.sumOf { i ->
        abs(if((matrixA[i] > 3 && matrixB[i] <= 3) || (matrixB[i] > 3 && matrixA[i] <= 3)) 1 else 0)
    }
}

fun calculateDifference3(matrixA: IntArray, matrixB: IntArray): Int {
    return matrixA.indices.sumOf { i ->
        abs(if((matrixA[i] != matrixB[i])) 1 else 0)
    }
}

 */
