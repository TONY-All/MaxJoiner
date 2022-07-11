data class ComparableDC(val name: String, val id: Int) : Comparable<ComparableDC> {
    override fun compareTo(other: ComparableDC): Int {
        return this.id - other.id
    }
}

fun main() {
    val list = ArrayList<ComparableDC>(listOf(ComparableDC("a", 1), ComparableDC("b", 2)))

    list.sort()
    list.sortDescending()

    list.forEach {
        println(it.name)
    }

}
