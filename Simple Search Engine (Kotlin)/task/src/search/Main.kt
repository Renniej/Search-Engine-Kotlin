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

class Database {

    private val database = mutableListOf<List<String>>()


    fun addEntry(entry : List<String>){
        database += entry
    }

    fun printEntry(entry : List<String>) {
        println(entry.joinToString(separator = " "))
    }
    
    fun printAll() {
        for (entry in database) printEntry(entry)
    }

    fun query(term : String, caseInsenitive : Boolean = true) : List<List<String>> {
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

        for (line in file.readLines()) {
            addEntry(line.split(" ").map { it.filter { !it.isWhitespace()} }) // splits entries into first name, last name, email and then removes any excess whitespace in each entry
        }
    }

}


class DatabaseMenu(val db: Database) {

    fun addMultipleEntries() {

        val numOfEntries = getNumOfEntries()

        println("Enter all people:")

        for (i in 1..numOfEntries) {
            db.addEntry(readln().split(" ").map { it.filter { !it.isWhitespace()} }) // splits entries into first name, last name, email and then removes any excess whitespace in each entry
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

    val db = Database()
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
