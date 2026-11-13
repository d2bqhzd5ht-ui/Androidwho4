package cn.stu.edu.sicnu.tay.bookmanager

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * 第 1 个 Activity：以列表形式显示所有书籍，点击后跳转到详情页
 */
class MainActivity : AppCompatActivity() {

    /**
     * 在代码中创建多个书籍对象并放入数组
     */
    private val books = arrayOf(
        Book("第一行代码（第3版）", "郭霖", "人民邮电出版社", 79.00, "9787115524836", 2020),
        Book("Java核心技术 卷I", "Cay S. Horstmann", "机械工业出版社", 149.00, "9787111623175", 2019),
        Book("Kotlin实战", "Dmitry Jemerov", "电子工业出版社", 89.00, "9787121320491", 2017),
        Book("计算机网络（第8版）", "谢希仁", "电子工业出版社", 49.00, "9787121410833", 2021),
        Book("算法（第4版）", "Robert Sedgewick", "人民邮电出版社", 99.00, "9787115293800", 2012),
        Book("数据库系统概论（第6版）", "王珊", "高等教育出版社", 45.00, "9787040511124", 2023),
        Book("深入理解计算机系统", "Randal E. Bryant", "机械工业出版社", 139.00, "9787111544937", 2016),
        Book("Android开发权威指南", "李刚", "人民邮电出版社", 89.00, "9787115436503", 2021)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.main_title)

        // 按价格从低到高排序
        val sortedBooks = books.sortedBy { it.price }

        val tipView = findViewById<TextView>(R.id.tv_tip)
        tipView.text = getString(R.string.list_tip, sortedBooks.size)

        val listView = findViewById<ListView>(R.id.lv_books)
        listView.adapter = BookAdapter(this, sortedBooks)
        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_BOOK, sortedBooks[position])
            startActivity(intent)
        }
    }
}
