package cn.stu.edu.sicnu.tay.lab06

import android.content.Context
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 个人信息序列化存储工具。
 *
 * 写文件：Context.openFileOutput + ObjectOutputStream
 * 读文件：Context.openFileInput  + ObjectInputStream
 */
object ProfileStore {

    private const val FILE_NAME = "profile_tay.dat"

    /**
     * 读取个人信息；首次运行或文件损坏时返回默认值。
     */
    fun load(context: Context): Profile {
        return try {
            context.openFileInput(FILE_NAME).use { input ->
                ObjectInputStream(input).use { stream ->
                    stream.readObject() as Profile
                }
            }
        } catch (e: Exception) {
            Profile()
        }
    }

    /**
     * 把个人信息序列化写入应用私有文件。
     */
    fun save(context: Context, profile: Profile): Boolean {
        return try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                ObjectOutputStream(output).use { stream ->
                    stream.writeObject(profile)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
