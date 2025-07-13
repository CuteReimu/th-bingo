import org.tfcc.bingo.Spell
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.HashMap
import kotlin.random.asKotlinRandom

class RefreshSpellManager {

    // 1) 一个HashMap<Boolean, HashMap<Int, LinkedList<Spell>>>存储在内部（记为A）。
    // 使用 private val 确保其内部性，并使用 HashMap() 初始化。
    // 注意：这里使用 var 是为了在 initialize 方法中重新赋值，
    // 如果 initialize 只调用一次且在 object 初始化时传入，则可以是 val。
    // 但通常 object 的初始化方法是外部调用的，所以 var 更灵活。
    private val storageA: HashMap<Boolean, HashMap<Int, LinkedList<Spell>>> = HashMap()

    // 2) 一个空的HashMap<Boolean, HashMap<Int, LinkedList<Spell>>>（记为B）。
    private val storageB: HashMap<Boolean, HashMap<Int, LinkedList<Spell>>> = HashMap()

    // 用于随机选择的随机数生成器
    private val random = ThreadLocalRandom.current().asKotlinRandom()

    /**
     * 初始化 SpellManager，将传入的数据深度拷贝到 storageA。
     * 构造时会直接传入一个结构相同的元素，直接进行拷贝。
     *
     * @param initialData 用于初始化 storageA 的数据。
     */
    fun init(initialData: HashMap<Boolean, HashMap<Int, LinkedList<Spell>>>) {
        reset()
        initialData.forEach { (boolKey, innerMap) ->
            // 深度拷贝内层 HashMap 和 LinkedList
            storageA[boolKey] = innerMap.mapValuesTo(HashMap()) { (_, list) ->
                LinkedList(list) // LinkedList(collection) 会创建一个新的 LinkedList 并拷贝所有元素
            }
        }
    }

    private fun reset() {
        storageA.clear()
        storageB.clear()
    }

    /**
     * 根据输入的 Spell 执行核心逻辑。
     *
     * @param inputSpell 输入的 Spell 对象。
     * @return 找到并处理后的 Spell 对象，如果找不到则返回 null。
     */
    fun refreshSpell(inputSpell: Spell): Spell? {
        // 1) 输入一个Spell，根据该Spell的rank是否equals("L")，对A进行第一次查询。
        // 特别地，若判断结果为false，则查询第一层为true的元素，为true则反之。
        // 即：如果 rank == "L" (true)，则查询 key 为 false 的；
        //     如果 rank != "L" (false)，则查询 key 为 true 的。
        val primaryBoolKey = (inputSpell.rank != "L") // 如果 rank 不是 "L"，则为 true；否则为 false。

        val star = inputSpell.star

        var resultSpell: Spell? = null

        // 尝试从 storageA 中获取 Spell 列表
        val aInnerMap = storageA[primaryBoolKey]
        val aSpellList = aInnerMap?.get(star)

        // 2) 从中随机返回一个元素。
        if (!aSpellList.isNullOrEmpty()) {
            resultSpell = aSpellList.random(random) // 从非空列表中随机取一个元素
        } else {
            // 3) 若对应的列表为空，则改为对B进行查询。
            val bInnerMap = storageB[primaryBoolKey]
            val bSpellList = bInnerMap?.get(star)

            // 查询到B的列表后，若B的列表为空，直接返回null。该流程结束。
            if (bSpellList.isNullOrEmpty()) {
                return null // A 和 B 对应的列表都为空，流程结束，返回 null
            } else {
                // 4) 若查询到B内的列表不为空，则从该列表中随机取一个元素返回，
                resultSpell = bSpellList.random(random)

                // 并将该列表复制到A中。
                val targetAInnerMap = storageA.getOrPut(primaryBoolKey) { HashMap() }
                targetAInnerMap[star] = LinkedList(bSpellList) // 深度拷贝 B 的列表到 A

                // 随后清空B内对应的列表。
                bSpellList.clear()
            }
        }

        // 5) 将返回的元素从A中移除，将输入的Spell存储到B中。该流程结束。
        // 从 A 中移除返回的元素
        // 注意：这里移除的是 resultSpell，它的 rank 决定了它在 A 中的位置。
        // 如果 resultSpell 是从 B 复制到 A 的，那么它现在就在 A 中了。
        storageA[primaryBoolKey]?.get(resultSpell.star)?.remove(resultSpell)

        // 将输入的 Spell 存储到 B 中
        val targetBInnerMap = storageB.getOrPut(primaryBoolKey) { HashMap() }
        val targetBList = targetBInnerMap.getOrPut(inputSpell.star) { LinkedList() }
        targetBList.add(inputSpell)

        return resultSpell
    }
}
