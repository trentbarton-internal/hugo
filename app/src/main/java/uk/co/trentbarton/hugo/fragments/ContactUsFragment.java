package uk.co.trentbarton.hugo.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.List;
import uk.co.trentbarton.hugo.R;

public class ContactUsFragment extends Fragment {

    ImageView facebookIcon, twitterIcon, instagramIcon;
    LinearLayout callButton, emailButton, websiteButton;
    private static String FACEBOOK_URL = "https://www.facebook.com/trentbartonland";
    private static String FACEBOOK_PAGE_ID = "trentbartonland";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);

        facebookIcon = view.findViewById(R.id.facebook_button);
        twitterIcon = view.findViewById(R.id.twitter_button);
        instagramIcon = view.findViewById(R.id.insta_button);
        callButton = view.findViewById(R.id.call_us_button);
        emailButton = view.findViewById(R.id.email_button);
        websiteButton = view.findViewById(R.id.website_button);

        callButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:01773712265"));
                startActivity(intent);
            } catch (Exception e) {
            }
        });

        emailButton.setOnClickListener(v -> {
            try {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:talk@trentbarton.co.uk"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "hugo app help and support");
                startActivity(emailIntent);
            } catch (Exception e) {
            }

        });

        websiteButton.setOnClickListener(v -> {
            try {
                String url = "http://www.trentbarton.co.uk";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
            }

        });

        facebookIcon.setOnClickListener(v -> {
            try {
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                String facebookUrl = getFacebookPageURL(getActivity());
                facebookIntent.setData(Uri.parse(facebookUrl));
                startActivity(facebookIntent);
            } catch (Exception e) {
            }

        });

        twitterIcon.setOnClickListener(v -> {
            Intent intent;
            try {
                // get the Twitter app if possible
                getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=202776817"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } catch (Exception e) {
                // no Twitter app, revert to browser
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/trentbartonland"));
            }
            startActivity(intent);
        });

        instagramIcon.setOnClickListener(v -> {
            Uri uri = Uri.parse("http://instagram.com/_u/trentbartonland");
            Intent insta = new Intent(Intent.ACTION_VIEW, uri);
            insta.setPackage("com.instagram.android");

            if (isIntentAvailable(getActivity(), insta)) {
                startActivity(insta);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/trentbartonland/")));
            }
        });

        return view;

    }

    private String getFacebookPageURL(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }


    private boolean isIntentAvailable(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
