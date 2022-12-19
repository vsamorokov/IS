package me.vsamorokov.searchengine.util

fun <V> product(lists: List<List<V>>): List<List<V>> {
    val product: MutableList<List<V>> = ArrayList()

    // We first create a list for each value of the first list
    product(product, ArrayList(), lists)
    return product
}

private fun <V> product(
    result: MutableList<List<V>>,
    existingTupleToComplete: List<V>,
    valuesToUse: List<List<V>>
) {
    for (value in valuesToUse[0]) {
        val newExisting: MutableList<V> = ArrayList(existingTupleToComplete)
        newExisting.add(value)

        // If only one column is left
        if (valuesToUse.size == 1) {
            // We create a new list with the exiting tuple for each value with the value
            // added
            result.add(newExisting)
        } else {
            // If there are still several columns, we go into recursion for each value
            val newValues: MutableList<List<V>> = ArrayList()
            // We build the next level of values
            for (i in 1 until valuesToUse.size) {
                newValues.add(valuesToUse[i])
            }
            product(result, newExisting, newValues)
        }
    }
}
