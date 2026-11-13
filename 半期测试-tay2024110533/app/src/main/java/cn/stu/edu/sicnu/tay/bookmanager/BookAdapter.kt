package cn.stu.edu.sicnu.tay.bookmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * 书籍列表适配器，负责把书籍数据绑定到列表项布局
 */
class BookAdapter(
    private val context: Context,
    private val books: List<Book>
) : BaseAdapter() {

    override fun getCount(): Int = books.size

    override fun getItem(position: Int): Book = books[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val book = books[position]
        holder.title.text = book.title
        holder.author.text = context.getString(R.string.item_author, book.author)
        holder.publisher.text = book.publisher
        holder.price.text = context.getString(R.string.price_format, book.price)
        return view
    }

    private class ViewHolder(view: View) {
        val title: TextView = view.findViewById(R.id.tv_book_title)
        val author: TextView = view.findViewById(R.id.tv_book_author)
        val publisher: TextView = view.findViewById(R.id.tv_book_publisher)
        val price: TextView = view.findViewById(R.id.tv_book_price)
    }
}
