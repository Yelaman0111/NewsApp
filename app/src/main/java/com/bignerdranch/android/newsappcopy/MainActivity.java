package com.bignerdranch.android.newsappcopy;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bignerdranch.android.newsappcopy.api.ApiClient;
import com.bignerdranch.android.newsappcopy.api.ApiInterface;
import com.bignerdranch.android.newsappcopy.models.Article;
import com.bignerdranch.android.newsappcopy.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    public static final String API_KEY = "9195795ae4cc45cb8bc395bc09fc6fb4";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>(  );
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        swipeRefreshLayout = findViewById( R.id.swipe_refresh_layout );
        swipeRefreshLayout.setOnRefreshListener( this );
        swipeRefreshLayout.setColorSchemeResources( R.color.colorAccent );

        recyclerView = findViewById( R.id.recyclerView );
        layoutManager = new LinearLayoutManager( MainActivity.this );
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setItemAnimator( new DefaultItemAnimator() );
        recyclerView.setNestedScrollingEnabled( false );
        onLoadingSwipeRefresh( "" );
    }

    public void LoadJson(){
        swipeRefreshLayout.setRefreshing( true );

        ApiInterface apiInterface = ApiClient.getApiClient().create( ApiInterface.class );

        String country = Utils.getCountry();
        String languge = Utils.getLanguage();

        Call<News> call;
        call = apiInterface.getNews(country ,API_KEY );

        call.enqueue( new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticle() != null){

                    if(!articles.isEmpty()){
                        articles.clear();
                    }

                    articles = response.body().getArticle();
                    adapter = new Adapter( articles, MainActivity.this );
                    recyclerView.setAdapter( adapter );
                    adapter.notifyDataSetChanged();

                    initListener();

                    swipeRefreshLayout.setRefreshing( false );

                }else{
                    swipeRefreshLayout.setRefreshing( false );
                    Toast.makeText( MainActivity.this, "No Results", Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                swipeRefreshLayout.setRefreshing( false );
            }
        } );

    }

    private void initListener(){
        adapter.setOnItemClickListener( new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent( MainActivity.this, NewsDetailActivity.class );

                Article article = articles.get(position);
                intent.putExtra( "url", article.getUrl() );
                intent.putExtra( "title", article.getTitle() );
                intent.putExtra( "img", article.getUrlToImage() );
                intent.putExtra( "date", article.getPublishedAt() );
                intent.putExtra( "source", article.getSource().getName());
                intent.putExtra( "author", article.getAuthor() );

                startActivity(intent);

            }
        } );
    }

    @Override
    public void onRefresh() {
        LoadJson();
    }

    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                LoadJson();
            }
        } );
    }
}
