package cn.stu.edu.sicnu.tay.bookmanager

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * 第 2 个 Activity：显示被选中书籍的完整信息
 */
class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK = "extra_book"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.detail_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val book = readBook()
        if (book == null) {
            finish()
            return
        }
        showBook(book)
    }

    private fun readBook(): Book? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_BOOK, Book::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_BOOK) as? Book
        }

    private fun showBook(book: Book) {
        findViewById<TextView>(R.id.tv_book_title).text = book.title
        findViewById<TextView>(R.id.tv_book_author).text = book.author
        findViewById<TextView>(R.id.tv_book_publisher).text = book.publisher
        findViewById<TextView>(R.id.tv_book_price).text = getString(R.string.price_format, book.price)
        findViewById<TextView>(R.id.tv_book_isbn).text = book.isbn
        findViewById<TextView>(R.id.tv_book_year).text = getString(R.string.year_format, book.publishYear)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
