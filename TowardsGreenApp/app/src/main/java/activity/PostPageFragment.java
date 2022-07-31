package activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.R;
import com.aueb.towardsgreen.Request;
import com.aueb.towardsgreen.Post;
import com.aueb.towardsgreen.asynctask.FetchDataAsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PostPageFragment extends Fragment {

    private boolean noMorePosts = false;
    private boolean refreshing = false;
    private int numberOfPostsFetched = 0;
    private LinearLayout postsLayout;
    private ScrollView postScrollView;
    private SwipeRefreshLayout postSwipeRefreshLayout;

    private FloatingActionButton floatingCreatePostBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postsLayout = view.findViewById(R.id.postsLayout);
        postScrollView =  view.findViewById(R.id.post_page_scrollView);
        postSwipeRefreshLayout = view.findViewById(R.id.post_page_refreshLayout);

        floatingCreatePostBtn = view.findViewById(R.id.post_create_floating_btn);

        floatingCreatePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction().replace(R.id.container_content, new CreatePostFragment()).commit();
            }
        });

        postSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                RefreshPostAsyncTask refreshPostAsyncTask = new RefreshPostAsyncTask(String.valueOf(numberOfPostsFetched), "GETPOSTS",
                        Post.class);
                refreshPostAsyncTask.execute();
            }
        });

        postScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View postView = postScrollView.getChildAt(postScrollView.getChildCount()-1);
                int scrollBottom = postView.getBottom() - (postScrollView.getHeight()+ postScrollView.getScrollY());

                if(scrollBottom == 0 && !noMorePosts){
                    FetchPostAsyncTask fetchPostAsyncTask = new FetchPostAsyncTask(getActivity(), "Φόρτωση δημοσιεύσεων. Παρακαλώ περιμένετε..."
                            , String.valueOf(numberOfPostsFetched), "GETMOREPOSTS", Post.class);
                    fetchPostAsyncTask.execute();
                }
            }
        });

        postScrollView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        FetchPostAsyncTask fetchPostAsyncTask = new FetchPostAsyncTask(getActivity(), "Φόρτωση περισσοτέρων δημοσιεύσεων. Παρακαλώ περιμένετε..."
                , String.valueOf(numberOfPostsFetched), "GETMOREPOSTS", Post.class);
        fetchPostAsyncTask.execute();
    }


    public void showPosts(ArrayList<Post> posts){
        boolean flag = true;
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        PostFragment postFragment;
        for(Post post: posts){
            Bundle bundle = new Bundle();
            bundle.putSerializable("post", post);
            postFragment = new PostFragment();
            postFragment.setArguments(bundle);
            if (flag && refreshing) {
                transaction.replace(postsLayout.getId(), postFragment);
                transaction.addToBackStack(null);
                flag = false;
            }
            else{
                transaction.add(postsLayout.getId(), postFragment);
            }
        }
        transaction.commit();
    }

    private class FetchPostAsyncTask extends FetchDataAsyncTask<Post> {

        public FetchPostAsyncTask(Context context, String message, String json, String requestType, Class<Post> classType) {
            super(context, message, json, requestType, classType);
        }

        @Override
        protected void onPostExecute(ArrayList<Post> requestedPosts) {
            super.onPostExecute(requestedPosts);
            numberOfPostsFetched += requestedPosts.size();
            if (requestedPosts.size() < 2) {
                noMorePosts = true;
            }
            showPosts(requestedPosts);
        }
    }

    private class RefreshPostAsyncTask extends FetchDataAsyncTask<Post> {

        public RefreshPostAsyncTask(String json, String requestType, Class<Post> classType) {
            super(json, requestType, classType);
        }

        @Override
        protected void onPostExecute(ArrayList<Post> requestedPosts) {
            super.onPostExecute(requestedPosts);
            showPosts(requestedPosts);
            refreshing = false;
            postSwipeRefreshLayout.setRefreshing(false);
        }
    }
}