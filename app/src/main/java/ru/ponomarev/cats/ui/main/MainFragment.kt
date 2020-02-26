package ru.ponomarev.cats.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import dagger.Lazy
import kotlinx.android.synthetic.main.main_fragment.*
import ru.ponomarev.cats.MainApp
import ru.ponomarev.cats.R
import javax.inject.Inject

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {
    fun updateCats(cats: List<CatVO>)
}

class MainFragment : MvpAppCompatFragment(), MainView {

    companion object {
        private const val PAGE_THRESHOLD = 8
        private const val PERMISSION_REQUEST_CODE = 32423
        private const val KEY_LAST_DOWNLOAD_URL = "lastDownloadUrl"

        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var pMainPresenter: Lazy<MainPresenter>
    @InjectPresenter
    lateinit var presenter: MainPresenter

    @ProvidePresenter
    fun providePresenter(): MainPresenter = pMainPresenter.get()

    private lateinit var lastDownloadUrl: String
    private lateinit var catsAdapter: CatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        MainApp.getComponent().inject(this)
        super.onCreate(savedInstanceState)
        lastDownloadUrl = savedInstanceState?.getString(KEY_LAST_DOWNLOAD_URL) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.main_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        catsAdapter = CatsAdapter(
            onDownload = ::tryDownloadCat,
            onFavorite = presenter::setFavoriteCat,
            isLoaderVisible = presenter::isCatsLoading
        )
        with(catsRv) {
            adapter = catsAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(
                PaginationScrollListener(
                    threshold = PAGE_THRESHOLD,
                    layoutManager = layoutManager as LinearLayoutManager,
                    canLoad = presenter::canLoadCats,
                    isLoading = presenter::isCatsLoading,
                    loadMoreItems = presenter::loadCats
                )
            )
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.favorites -> {
                with(item) {
                    isChecked
                        .let { showFavorite -> !showFavorite }
                        .let { showFavorite ->
                            isChecked = showFavorite
                            icon = ContextCompat.getDrawable(
                                requireContext(),
                                if (showFavorite) R.drawable.ic_favorite_24dp else R.drawable.ic_unfavorite_24dp
                            )
                            presenter.showCats(onlyFavorites = showFavorite)
                        }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_LAST_DOWNLOAD_URL, lastDownloadUrl)
    }

    override fun updateCats(cats: List<CatVO>) {
        catsAdapter.submitList(cats)
    }

    private fun tryDownloadCat(url: String) {
        lastDownloadUrl = url
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            presenter.downloadCat(url)
        }
    }

    private fun checkPermission(permissionName: String, onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                permissionName
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(permissionName), PERMISSION_REQUEST_CODE)
        } else {
            onGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tryDownloadCat(lastDownloadUrl)
            }
        }
    }
}

class PaginationScrollListener(
    private val threshold: Int,
    private val layoutManager: LinearLayoutManager,
    private val loadMoreItems: () -> Unit,
    private val canLoad: () -> Boolean,
    private val isLoading: () -> Boolean

) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (isLoading() || canLoad().not())
            return

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
            firstVisibleItemPosition >= 0 &&
            totalItemCount >= threshold
        ) {
            loadMoreItems.invoke()
        }
    }
}

