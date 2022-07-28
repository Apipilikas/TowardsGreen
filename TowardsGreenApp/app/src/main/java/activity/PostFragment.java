package activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aueb.towardsgreen.Connection;
import com.aueb.towardsgreen.R;
import com.aueb.towardsgreen.Request;
import com.aueb.towardsgreen.Post;
import com.aueb.towardsgreen.Profile;
import com.google.gson.Gson;

public class PostFragment extends Fragment {

    private Post post;
    private Profile profile = Connection.getInstance().getProfile();
    private boolean isSupervisor = false;

    private ImageView postMenu;

    private TextView post_description;
    private TextView post_title;
    private TextView publish_date;
    private TextView publish_time;
    private TextView publisher;

    private TextView agree;
    private TextView disagree;

    private ImageView userImg;
    private ImageView postImg;

    private MenuItem editItem;
    private MenuItem deleteItem;
    private MenuItem createFromPostItem;

    private boolean[] userReactions;
    private String[] reactionNames = {"Agree", "Disagree"};
    private TextView[] reactionsNumber;
    private LinearLayout[] reactionsLayout;
    private TextView[] reactions;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            post = (Post) getArguments().getSerializable("post");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        postMenu = view.findViewById(R.id.ic_posts_menu);

        publisher = view.findViewById(R.id.post_publisher_username_txt);
        publish_date = view.findViewById(R.id.post_published_date_txt);
        publish_time =  view.findViewById(R.id.post_published_time_txt);
        post_description = view.findViewById(R.id.post_description);
        post_title = view.findViewById(R.id.post_title_txt);
        postImg = view.findViewById(R.id.post_image);
        agree = view.findViewById(R.id.post_reaction_agree);
        disagree = view.findViewById(R.id.post_reaction_disagree);

        if (post.getImage() == null) {
            postImg.setVisibility(View.GONE);
        }
        else {
            postImg.setImageBitmap(post.getImageBitmap());
        }

        publisher.setText(post.getCreator());
        publish_date.setText(post.getPublishedDate());
        publish_time.setText(post.getPublishedTime());
        post_description.setText(post.getDescription());
        post_title.setText(post.getTitle());

        userReactions = new boolean[]{post.hasReacted("Agree", profile.getUserID()),
                post.hasReacted("Disagree", profile.getUserID())};

        reactionsNumber = new TextView[]{view.findViewById(R.id.post_reaction_agree_number),
                view.findViewById(R.id.post_reaction_disagree_number)};

        reactionsLayout = new LinearLayout[]{view.findViewById(R.id.post_agree_layout),
                view.findViewById(R.id.post_disagree_layout)};

        reactions = new TextView[]{view.findViewById(R.id.post_reaction_agree),
                view.findViewById(R.id.post_reaction_disagree)};

        showReactionNumbers();

        setInitialReactions();

        for (int i = 0; i < 2; i++) {
            int finalI = i;
            reactions[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!hasClickedOtherReaction(finalI)) {
                        if (userReactions[finalI]) {
                            userReactions[finalI] = false;
                            changeReactionColor(reactionsLayout[finalI], reactionsNumber[finalI], true);
                            post.removeReaction(reactionNames[finalI], profile.getUserID());
                            String reaction = post.getUsersAndReactions().get(profile.getUserID());
                            post.getUsersAndReactions().remove(profile.getUserID());
                            post.getProperReactionMap(reaction).remove(profile.getUserID());
                        }
                        else {
                            userReactions[finalI] = true;
                            changeReactionColor(reactionsLayout[finalI], reactionsNumber[finalI], false);
                            post.addReaction(reactionNames[finalI], profile.getUserID());
                            post.getUsersAndReactions().put(profile.getUserID(),reactionNames[finalI]);
                            String reaction = post.getUsersAndReactions().get(profile.getUserID());
                            post.getProperReactionMap(reaction).put(profile.getUserID(),profile.getFullName());
                        }
                        showReactionNumbers();
                        updatePostReaction();

                    }
                }
            });
        }

        postMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.post_menu_edit:
                                editPost();
                                break;
                            case R.id.post_menu_delete:
                                deletePost();
                                break;
                            case R.id.post_menu_create_event:
                                createEventFromPost();
                                break;
                            case R.id.post_menu_show_reactions:
                                showAttendeesDialog();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                popupMenu.show();
                Menu menu = popupMenu.getMenu();
                editItem = menu.findItem(R.id.post_menu_edit);
                deleteItem = menu.findItem(R.id.post_menu_delete);
                createFromPostItem = menu.findItem(R.id.post_menu_create_event);
                setUserMenu();
            }
        });
    }

    private void setUserMenu() {
        if (profile.getRole() == Profile.ROLE.SUPERVISOR) {
            isSupervisor = true;
            createFromPostItem.setVisible(true);
        }
        if (post.getCreatorID().equals(profile.getUserID())) {
            deleteItem.setVisible(true);
            editItem.setVisible(true);
        }
    }

    private void createEventFromPost() {
        CreateEditEventFragment createEditEventFragment = new CreateEditEventFragment();
        Bundle args = new Bundle();
        args.putString("mode", "createFromPost");
        args.putSerializable("post", post);
        createEditEventFragment.setArguments(args);
        getParentFragmentManager().beginTransaction().replace(R.id.container_content, createEditEventFragment).commit();
    }

    private boolean hasClickedOtherReaction(int reaction) {
        for (int i = 0; i < 2; i++) {
            if (userReactions[i] && (i != reaction)) {
                return true;
            }
        }
        return  false;
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
        for (int i = 0; i < 2; i++) {
            if(userReactions[i]) {
                changeReactionColor(reactionsLayout[i], reactionsNumber[i], false);
            }
        }
    }

    private void showReactionNumbers() {
        reactionsNumber[0].setText(String.valueOf(post.getAgreeNumberOfReactions()));
        reactionsNumber[1].setText(String.valueOf(post.getDisagreeNumberOfReactions()));
    }


    private void updatePostReaction() {
        Gson gson = new Gson();
        String updatedPost;

        updatedPost = gson.toJson(new Post(post.getReactions(),post.getUsersAndReactions(),post.getAgree(),post.getDisagree()));

        String json = gson.toJson(new String[]{post.getPostID(),updatedPost});
        Connection.getInstance().requestSendDataWithoutResponse(new Request("UPPOSTWR", json));
    }

    private void editPost() {
        CreatePostFragment createPostFragment = new CreatePostFragment();
        Bundle args = new Bundle();
        args.putSerializable("post",post);
        createPostFragment.setArguments(args);
        getParentFragmentManager().beginTransaction().replace(R.id.container_content, createPostFragment).commit();
    }

    private void deletePost() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                       PostFragment.DeletePostAsyncTask deletePostAsyncTask = new PostFragment.DeletePostAsyncTask();
                        deletePostAsyncTask.execute();
                        break;
                    case  DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eίσαι σίγουρος ότι θες να διαγράψεις την δημοσίευση;")
                .setPositiveButton(R.string.yes_label, dialogClickListener)
                .setNegativeButton(R.string.no_label, dialogClickListener).show();
    }

    private void showAttendeesDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_post_reactions);


        String[] arrayAgree = post.getAgree().values().toArray(new String[0]);
        String[] arrayDisagree = post.getDisagree().values().toArray(new String[0]);

        ListView listViewAgree = dialog.findViewById(R.id.post_menu_dialog_agree_reaction_list);
        ArrayAdapter<String> arrayAdapterAgree = new ArrayAdapter<String>(getContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                arrayAgree);
        listViewAgree.setAdapter(arrayAdapterAgree);

        ListView listViewDisagree = dialog.findViewById(R.id.post_menu_dialog_disagree_reaction_list);
        ArrayAdapter<String> arrayAdapterDisagree = new ArrayAdapter<String>(getContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                arrayDisagree);
        listViewDisagree.setAdapter(arrayAdapterDisagree);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void showAlertDialog(boolean result) {
        String successMessage = "Η δημοσίευση διαγράφθηκε επιτυχώς!";
        String failureMessage = "Κάποιο σφάλμα προέκυψε.";

        AlertDialog alertDialog;
        AlertDialog.Builder builderDialog = new AlertDialog.Builder(getActivity());
        View layoutView = null;

        if (result) {
            layoutView = getLayoutInflater().inflate(R.layout.success_dialog, null);
            TextView successMsg = layoutView.findViewById(R.id.success_dialog_txt);
            successMsg.setText(successMessage);
        } else {
            layoutView = getLayoutInflater().inflate(R.layout.failure_dialog, null);
            TextView failureMsg = layoutView.findViewById(R.id.failure_dialog_txt);
            failureMsg.setText(failureMessage);
        }

        builderDialog.setView(layoutView);

        alertDialog = builderDialog.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                alertDialog.dismiss();
                if (result) {
                    getParentFragmentManager().beginTransaction().remove(PostFragment.this).commit();
                }
            }
        }, 3000);
    }

    private class DeletePostAsyncTask extends AsyncTask<String, String, Boolean> {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Παρακαλώ περιμένετε...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return Connection.getInstance().requestSendData(new Request("DELPOST", post.getPostID()));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.hide();
            progressDialog.dismiss();
            showAlertDialog(result);
        }
    }
}
