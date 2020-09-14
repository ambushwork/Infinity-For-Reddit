package ml.docilealligator.infinityforreddit.BottomSheetFragment;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.CommentFullMarkdownActivity;
import ml.docilealligator.infinityforreddit.Activity.EditCommentActivity;
import ml.docilealligator.infinityforreddit.Activity.ReportActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.Comment.Comment;
import ml.docilealligator.infinityforreddit.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentMoreBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT = "ECF";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_POSITION = "EP";
    public static final String EXTRA_COMMENT_MARKDOWN = "ECM";
    public static final String EXTRA_IS_NSFW = "EIN";
    @BindView(R.id.edit_text_view_comment_more_bottom_sheet_fragment)
    TextView editTextView;
    @BindView(R.id.delete_text_view_comment_more_bottom_sheet_fragment)
    TextView deleteTextView;
    @BindView(R.id.save_text_view_comment_more_bottom_sheet_fragment)
    TextView shareTextView;
    @BindView(R.id.copy_text_view_comment_more_bottom_sheet_fragment)
    TextView copyTextView;
    @BindView(R.id.view_full_markdown_text_view_comment_more_bottom_sheet_fragment)
    TextView viewFullMarkdownTextView;
    @BindView(R.id.report_view_comment_more_bottom_sheet_fragment)
    TextView reportTextView;
    @BindView(R.id.see_removed_view_comment_more_bottom_sheet_fragment)
    TextView seeRemovedTextView;
    private AppCompatActivity activity;

    public CommentMoreBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comment_more_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        Bundle bundle = getArguments();
        if (bundle == null) {
            dismiss();
            return rootView;
        }
        Comment comment = bundle.getParcelable(EXTRA_COMMENT);
        if (comment == null) {
            dismiss();
            return rootView;
        }
        String accessToken = bundle.getString(EXTRA_ACCESS_TOKEN);

        if (accessToken != null && !accessToken.equals("")) {
            editTextView.setVisibility(View.VISIBLE);
            deleteTextView.setVisibility(View.VISIBLE);

            editTextView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, EditCommentActivity.class);
                intent.putExtra(EditCommentActivity.EXTRA_ACCESS_TOKEN, accessToken);
                intent.putExtra(EditCommentActivity.EXTRA_FULLNAME, comment.getFullName());
                intent.putExtra(EditCommentActivity.EXTRA_CONTENT, comment.getCommentMarkdown());
                intent.putExtra(EditCommentActivity.EXTRA_POSITION, bundle.getInt(EXTRA_POSITION));
                if (activity instanceof ViewPostDetailActivity) {
                    activity.startActivityForResult(intent, ViewPostDetailActivity.EDIT_COMMENT_REQUEST_CODE);
                } else {
                    startActivity(intent);
                }

                dismiss();
            });

            deleteTextView.setOnClickListener(view -> {
                dismiss();
                if (activity instanceof ViewPostDetailActivity) {
                    ((ViewPostDetailActivity) activity).deleteComment(comment.getFullName(), bundle.getInt(EXTRA_POSITION));
                } else if (activity instanceof ViewUserDetailActivity) {
                    ((ViewUserDetailActivity) activity).deleteComment(comment.getFullName());
                }
            });
        }

        shareTextView.setOnClickListener(view -> {
            dismiss();
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, comment.getPermalink());
                activity.startActivity(Intent.createChooser(intent, getString(R.string.share)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
            }
        });

        copyTextView.setOnClickListener(view -> {
            dismiss();
            CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
            Bundle copyBundle = new Bundle();
            copyBundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, comment.getCommentMarkdown());
            copyBundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, comment.getCommentRawText());
            copyTextBottomSheetFragment.setArguments(copyBundle);
            copyTextBottomSheetFragment.show(activity.getSupportFragmentManager(), copyTextBottomSheetFragment.getTag());
        });

        viewFullMarkdownTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CommentFullMarkdownActivity.class);
            intent.putExtra(CommentFullMarkdownActivity.EXTRA_IS_NSFW, bundle.getBoolean(EXTRA_IS_NSFW, false));
            intent.putExtra(CommentFullMarkdownActivity.EXTRA_COMMENT_MARKDOWN, bundle.getString(EXTRA_COMMENT_MARKDOWN, ""));
            activity.startActivity(intent);

            dismiss();
        });

        reportTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, comment.getSubredditName());
            intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, comment.getFullName());
            activity.startActivity(intent);

            dismiss();
        });

        if ("[deleted]".equals(comment.getAuthor()) ||
                "[deleted]".equals(comment.getCommentRawText()) ||
                "[removed]".equals(comment.getCommentRawText())
        ) {
            seeRemovedTextView.setVisibility(View.VISIBLE);

            seeRemovedTextView.setOnClickListener(view -> {
                dismiss();
                if (activity instanceof ViewPostDetailActivity) {
                    ((ViewPostDetailActivity) activity).showRemovedComment(comment, bundle.getInt(EXTRA_POSITION));
                }
            });
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }
}
