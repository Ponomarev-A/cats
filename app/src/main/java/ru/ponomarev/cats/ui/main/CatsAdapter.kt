package ru.ponomarev.cats.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.cats_item.*
import kotlinx.android.synthetic.main.progress_item.*
import ru.ponomarev.cats.R

data class CatVO(
    val id: String,
    val url: String,
    val isFavorite: Boolean,
    val isSelected: Boolean = false
)

class CatsAdapter(
    private val onDownload: (String) -> Unit,
    private val onFavorite: (CatVO) -> Unit,
    private val isLoaderVisible: () -> Boolean
) : ListAdapter<CatVO, VH>(CatsDiffCallback) {

    companion object {
        private const val CATS_TYPE = 1
        private const val PROGRESS_TYPE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CATS_TYPE -> CatViewHolder(
                containerView = inflater.inflate(R.layout.cats_item, parent, false),
                onDownload = onDownload,
                onFavorite = onFavorite
            )
            PROGRESS_TYPE -> ProgressViewHolder(
                containerView = inflater.inflate(R.layout.progress_item, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (position == itemCount - 1 && isLoaderVisible()) {
            PROGRESS_TYPE
        } else {
            CATS_TYPE
        }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[position])
    }
}

object CatsDiffCallback : DiffUtil.ItemCallback<CatVO>() {
    override fun areItemsTheSame(oldItem: CatVO, newItem: CatVO): Boolean =
        oldItem.id == newItem.id

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: CatVO, newItem: CatVO): Boolean =
        oldItem == newItem
}


abstract class VH(
    override val containerView: View
) :
    RecyclerView.ViewHolder(containerView), LayoutContainer {

    abstract fun bind(item: CatVO)
}

class CatViewHolder(
    override val containerView: View,
    onDownload: (String) -> Unit,
    onFavorite: (CatVO) -> Unit
) : VH(containerView) {

    private lateinit var catVO: CatVO

    init {
        downloadBtn.setOnClickListener { onDownload(catVO.url) }
        downloadBtn.setImageDrawable(
            ContextCompat.getDrawable(
                containerView.context,
                R.drawable.ic_file_download_black_24dp
            )
        )

        favoriteBtn.setOnClickListener { onFavorite(catVO) }
        favoriteBtn.setImageDrawable(
            ContextCompat.getDrawable(
                containerView.context,
                R.drawable.ic_favorite_selector
            )
        )
    }

    override fun bind(item: CatVO) {
        this.catVO = item
        Glide.with(imageIv).load(catVO.url).into(imageIv)
        favoriteBtn.isSelected = catVO.isFavorite
    }
}

class ProgressViewHolder(override val containerView: View) : VH(containerView) {

    override fun bind(item: CatVO) {
        progressBarFl.visibility = View.VISIBLE
    }
}