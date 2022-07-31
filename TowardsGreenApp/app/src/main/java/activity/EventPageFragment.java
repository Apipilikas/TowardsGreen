package activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.Event;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.aueb.towardsgreen.R;
import com.aueb.towardsgreen.Profile;
import com.aueb.towardsgreen.asynctask.FetchDataAsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class EventPageFragment extends Fragment {
    private Profile profile;

    private boolean refreshing = false;
    private int numberOfEventsFetched = 0;
    private boolean noMoreEvents = false;

    private LinearLayout eventsLayout;
    private ScrollView eventScrollView;
    private SwipeRefreshLayout eventSwipeRefreshLayout;

    private FloatingActionButton floatingCreateEventBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profile = Connection.getInstance().getProfile();

        eventsLayout = view.findViewById(R.id.eventsLayout);
        eventScrollView = view.findViewById(R.id.event_page_scrollView);
        eventSwipeRefreshLayout = view.findViewById(R.id.event_page_refreshLayout);

        floatingCreateEventBtn = view.findViewById(R.id.event_create_floating_btn);

        if (profile.getRole() == Profile.ROLE.USER) {
            floatingCreateEventBtn.setVisibility(View.GONE);
        }

        floatingCreateEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction().replace(R.id.container_content, new CreateEditEventFragment()).commit();
            }
        });


        eventSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                RefreshEventAsyncTask refreshEventAsyncTask = new RefreshEventAsyncTask(String.valueOf(numberOfEventsFetched),
                        "GETEV", Event.class);
                refreshEventAsyncTask.execute();
            }
        });

        eventScrollView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        eventScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View sView = eventScrollView.getChildAt(eventScrollView.getChildCount() - 1);
                int scrollBottom = sView.getBottom() - (eventScrollView.getHeight() + eventScrollView.getScrollY());

                if (scrollBottom == 0 && !noMoreEvents) {
                    FetchEventAsyncTask fetchEventAsyncTask = new FetchEventAsyncTask(getActivity(), "Φόρτωση περισσοτέρων εκδηλώσεων. Παρακαλώ περιμένετε...",
                            String.valueOf(numberOfEventsFetched), "GETMOREEV", Event.class);
                    fetchEventAsyncTask.execute();
                }
            }
        });
        FetchEventAsyncTask fetchEventAsyncTask = new FetchEventAsyncTask(getActivity(), "Παρακαλώ περιμένετε...",
                String.valueOf(numberOfEventsFetched), "GETMOREEV", Event.class);
        fetchEventAsyncTask.execute();
    }

    private class FetchEventAsyncTask extends FetchDataAsyncTask<Event> {

        public FetchEventAsyncTask(Context context, String message, String json, String requestType, Class<Event> classType) {
            super(context, message, json, requestType, classType);
        }

        @Override
        protected void onPostExecute(ArrayList<Event> requestedEvents) {
            super.onPostExecute(requestedEvents);
            numberOfEventsFetched += requestedEvents.size();
            if (requestedEvents.size() < 2) {
                noMoreEvents = true;
            }
            showEvents(requestedEvents);
        }
    }

    private class RefreshEventAsyncTask extends FetchDataAsyncTask<Event> {


        public RefreshEventAsyncTask(String json, String requestType, Class<Event> classType) {
            super(json, requestType, classType);
        }

        @Override
        protected void onPostExecute(ArrayList<Event> requestedEvents) {
            super.onPostExecute(requestedEvents);
            showEvents(requestedEvents);
            refreshing = false;
            eventSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showEvents(ArrayList<Event> events) {
        boolean flag = true;
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        EventFragment eventFragment;
        for (Event event : events) {
            Bundle args = new Bundle();
            args.putSerializable("event", event);
            eventFragment = new EventFragment();
            eventFragment.setArguments(args);
            if (flag && refreshing) {
                transaction.replace(eventsLayout.getId(), eventFragment);
                transaction.addToBackStack(null);
                flag = false;
            } else {
                transaction.add(eventsLayout.getId(), eventFragment);
            }
        }
        transaction.commit();
    }
}