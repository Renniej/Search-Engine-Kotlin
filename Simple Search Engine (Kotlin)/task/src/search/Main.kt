package search

import java.awt.Menu
import java.io.File
import javax.management.Query
import javax.xml.crypto.Data

const val F_NAME_INDEX =  0
const val L_NAME_INDEX = 1
const val EMAIL_INDEX = 2

//look for substrings of query in search
//trim spaces out after parsing (delimiter), case insenseitive

enum class MenuOptions {
    QUERY,
    PRINT_ALL,
    EXIT,
    INVALID
}

class IndexedDatabase : Database() {

    private val invertedIndex = mutableMapOf<String,MutableList<Int>>()
    override fun addEntry(str : String){
        super.addEntry(str)
        for (field in database.last()) addInvertedIndex(field,database.lastIndex)
    }

    private fun addInvertedIndex(str : String, index : Int) {

        val duplicate : Int?
        val key = str.lowercase()

        if (!invertedIndex.contains(key)) {
            invertedIndex += key to mutableListOf(index)
        } else {
            duplicate = invertedIndex[key]?.find { it == index}
            if (duplicate == null) invertedIndex[key]?.add(index)
        }
    }


    override fun query(term : String) : List<List<String>> {

        val key = term.lowercase()

        return if (invertedIndex.contains(key))
            invertedIndex[key]!!.map{ database[it] }
        else
            emptyList()

    }


}

open class Database {

    protected  val database = mutableListOf<List<String>>()


    open fun addEntry(str : String) {
       val entry = str.split(" ").map { it.filter { !it.isWhitespace()} }// splits entries into first name, last name, email and then removes any excess whitespace in each entry
        database += entry
    }



    fun printEntry(entry : List<String>) {
        println(entry.joinToString(separator = " "))
    }
    
    fun printAll() {
        for (entry in database) printEntry(entry)
    }


    open fun query(term : String) : List<List<String>> {
        val query = Regex(term, RegexOption.IGNORE_CASE)
        return  (database.filter {

            var queryFound = false

            for (entry in it) {
                if (entry.contains(query)){
                  return@filter true
                }
            }

            return@filter false

        }).toList()
    }

    fun addEntriesFromFile(file : File) {
        if (!file.exists()) throw Error("File does not exist")
        file.readLines().forEach { addEntry(it) }
    }

}


class DatabaseMenu(val db: Database) {

    fun addMultipleEntries() {

        val numOfEntries = getNumOfEntries()

        println("Enter all people:")

        for (i in 1..numOfEntries) {
            db.addEntry(readln())
        }

    }

    private fun getNumOfEntries() : Int {
        println("Enter the number of people:")
        val numOfEntries = readln().toInt()
        if (numOfEntries < 0) throw error("Entries need to be over 0")
        return numOfEntries

    }


    fun printAll(){
        db.printAll()
    }


    fun printMenu() {
        println()
        println("=== Menu ===")
        println("1. Find a person")
        println("2. Print all people")
        println("0. Exit")
    }

    fun getMenuInput() : MenuOptions {

        val input = readln().toIntOrNull()

        return when (input){
            1 -> MenuOptions.QUERY
            2 -> MenuOptions.PRINT_ALL
            0 -> MenuOptions.EXIT
            else -> MenuOptions.INVALID
        }

    }

    fun inputQuery() {
        println("Enter a name or email to search all suitable people.")
        db.query(readln()).forEach { db.printEntry(it) }
    }


}




fun main(args : Array<String>) {

    val db = IndexedDatabase()
    val menu = DatabaseMenu(db)
    var mOption : MenuOptions

    db.addEntriesFromFile(File(args[1]))

    do {
        menu.printMenu()
        mOption = menu.getMenuInput()

        when(mOption) {
            MenuOptions.QUERY -> menu.inputQuery()
            MenuOptions.PRINT_ALL -> menu.printAll()
            MenuOptions.INVALID -> println("Incorrect option! Try again.")
            MenuOptions.EXIT -> println("\nBye!")
        }

    } while(mOption != MenuOptions.EXIT)

}
