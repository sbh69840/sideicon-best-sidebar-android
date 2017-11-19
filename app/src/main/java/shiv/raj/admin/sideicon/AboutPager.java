package shiv.raj.admin.sideicon;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import shiv.raj.admin.sideicon.AboutActivity.AboutPage;
import shiv.raj.admin.sideicon.AboutActivity.Element;


/**
 * Created by shivraj on 27/01/2017.
 */

public class AboutPager extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    Element adsElement = new Element();
    adsElement.setTitle("Advertise with me in previous fragment");

    return new AboutPage(getActivity())
            .setCustomFont("fonts/minimal.otf")
            .addItem(new Element().setTitle("Version 1"))
            .addItem(adsElement)
            .addGroup("Connect with me")
            .addEmail("sbh69840@gmail.com")
            .addGitHub("sbh69840")
            .addFacebook("shivrajbh")
            .addInstagram("shivraj_rdj")
            .addTwitter("technogoyt")
            .isRTL(false)
            .addWebsite("https://patreon.com/sbh")
            .create();

}
    }
