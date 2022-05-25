package activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.Event;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.aueb.towardsgreen.R;
import com.aueb.towardsgreen.Request;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EventFragment extends Fragment {
    // Requirement layout (LinearLayout) initialization
    private LinearLayout requirementLayout;

    // Event main fields (TextViews) initialization
    private TextView publishedTime;
    private TextView publishedDate;
    private TextView title;
    private TextView status;
    private TextView description;
    private TextView meetingDate;
    private TextView meetingTime;
    private TextView location;
    private TextView badge;

    // Event image(ImageView) initialization
    private ImageView eventMenu;

    // Event menu button (ImageView) initialization
    private ImageView eventImage;

    // Event Reactions names
    private String[] reactionNames = {"TakePart", "Maybe", "NotInterested"};

    // Event number of Reactions (TextViews) initialization
    private TextView[] reactionsNumber;

    // Event Reactions buttons (TextViews-button use) initialization
    private TextView[] reactions;

    //Event Reactions Layouts initialization
    private LinearLayout[] reactionsLayout;

    // Event Reactions table (show where user has reacted)
    private boolean[] userReactions;

    private Event event;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Requirement layout (LinearLayout) declaration
        requirementLayout = view.findViewById(R.id.event_requirementLayout);

        // Event main fields (TextViews) declaration
        TextView publisherUsername = view.findViewById(R.id.event_publisher_username_txt);
        publishedTime = view.findViewById(R.id.event_published_time_txt);
        publishedDate = view.findViewById(R.id.event_published_date_txt);
        title = view.findViewById(R.id.event_title_txt);
        status = view.findViewById(R.id.event_status_txt);
        description = view.findViewById(R.id.event_description_txt);
        meetingDate = view.findViewById(R.id.event_meeting_date_txt);
        meetingTime = view.findViewById(R.id.event_meeting_time_txt);
        location = view.findViewById(R.id.event_location_txt);
        badge = view.findViewById(R.id.event_badge_txt);

        // Event image (ImageView) declaration
        eventImage = view.findViewById(R.id.event_image);
        eventImage.setImageBitmap(event.getImageBitmap());

        // Event menu button (ImageView) declaration
        eventMenu = view.findViewById(R.id.ic_events_menu);

        // Event number of Reactions (TextViews) declaration
        reactionsNumber = new TextView[]{view.findViewById(R.id.event_reaction_takePart_number),
                                         view.findViewById(R.id.event_reaction_maybe_number),
                                         view.findViewById(R.id.event_reaction_notInterested_number)};

        // Event Reactions buttons (TextViews) declaration
        reactions = new TextView[]{view.findViewById(R.id.event_reaction_takePart),
                                   view.findViewById(R.id.event_reaction_maybe),
                                   view.findViewById(R.id.event_reaction_notInterested)};

        // Event Reactions Layouts declaration
        reactionsLayout = new LinearLayout[]{view.findViewById(R.id.event_takePartLayout),
                                             view.findViewById(R.id.event_maybeLayout),
                                             view.findViewById(R.id.event_notInterestedLayout)};

        // Setting Event main fields
        publisherUsername.setText(event.getCreator());
        publishedTime.setText(event.getPublishedTime().toString());
        publishedDate.setText(event.getPublishedDate().toString());
        title.setText(event.getTitle());
        status.setText(event.getStatusString());
        description.setText(event.getDescription());
        meetingDate.setText(event.getMeetingDate().toString());
        meetingTime.setText(event.getMeetingTime().toString());
        location.setText(event.getMeetingLocation());
        badge.setText(event.getBadge());
        showReactionNumbers();

        // Event user's reactions
        userReactions = new boolean[]{event.hasReacted("TakePart", "u101"),
                event.hasReacted("Maybe", "u101"),
                event.hasReacted("NotInterested", "u101")};

        // Setting Event requirements in the appropriate fragment
        FragmentTransaction transaction =getChildFragmentManager().beginTransaction();
        EventRequirementFragment eventRequirementFragment;

        if (!event.getRequirements().isEmpty()) {
            requirementLayout.setVisibility(View.VISIBLE);
            for (Map.Entry<String, Boolean> requirement : event.getRequirements().entrySet()) {
                Bundle args = new Bundle();
                args.putString("requirementName", requirement.getKey());
                args.putString("requirementFulfillment", String.valueOf(requirement.getValue()));
                eventRequirementFragment = new EventRequirementFragment();
                eventRequirementFragment.setArguments(args);
                transaction.add(requirementLayout.getId(), eventRequirementFragment);
            }
        }
        transaction.commit();

        setInitialReactions();

        // Menu button (3 dots) on Click Listener
        eventMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit:
                                Toast.makeText(getActivity(), "Edit", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.event_menu);
                popupMenu.show();
            }
        });

        // TakePart reaction on Click Listener
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            reactions[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!hasClickedOtherReaction(finalI)) {
                        if (userReactions[finalI]) {
                            userReactions[finalI] = false;
                            changeReactionColor(reactionsLayout[finalI], reactionsNumber[finalI], true);
                            event.removeReaction(reactionNames[finalI], "u101");
                        }
                        else {
                            userReactions[finalI] = true;
                            changeReactionColor(reactionsLayout[finalI], reactionsNumber[finalI], false);
                            event.addReaction(reactionNames[finalI], "u101");
                        }
                        showReactionNumbers();
                        updateEvent();
                    }
                }
            });
        }
    }

    private void changeReactionColor(LinearLayout linearLayout, TextView textView, boolean back) {
        int colorFrom = getResources().getColor(R.color.grey_200);
        int colorTo = getResources().getColor(R.color.green_200);
        if (back) {
            colorFrom = getResources().getColor(R.color.green_200);
            colorTo = getResources().getColor(R.color.grey_200);
        }
        ValueAnimator colorAnimatorLayout = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimatorLayout.setDuration(250);
        colorAnimatorLayout.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textView.setBackgroundColor(Color.parseColor("#D6D6D6"));
                linearLayout.setBackgroundColor((int) colorAnimatorLayout.getAnimatedValue());
            }
        });
        colorAnimatorLayout.start();

        ValueAnimator colorAnimatorText = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
        colorAnimatorText.setDuration(250);
        colorAnimatorText.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textView.setBackgroundColor((int) colorAnimatorText.getAnimatedValue());
            }
        });
        colorAnimatorText.start();
    }

    private void setInitialReactions() {
        for (int i = 0; i < 3; i++) {
            if(userReactions[i]) {
                changeReactionColor(reactionsLayout[i], reactionsNumber[i], false);
            }
        }
    }

    private void showReactionNumbers() {
        reactionsNumber[0].setText(String.valueOf(event.getTakePartNumberOfReactions()));
        reactionsNumber[1].setText(String.valueOf(event.getMaybeNumberOfReactions()));
        reactionsNumber[2].setText(String.valueOf(event.getNotInterestedNumberOfReactions()));
    }

    private boolean hasClickedOtherReaction(int reaction) {
        for (int i = 0; i < 3; i++) {
            if (userReactions[i] && (i != reaction)) {
                return true;
            }
        }
        return  false;
    }

    private void updateEvent() {
        String updatedEvent = new Gson().toJson(new Event(event.getReactions()));
        String json = new Gson().toJson(new String[]{event.getEventID(), updatedEvent});
        Connection.getInstance().requestSendData(new Request("UP", json));
    }
}