package shiv.raj.admin.sideicon.floatingfolders;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ActivityInfo;

class FolderModel {
	public int id;
	public String name;
	List<ActivityInfo> apps;
	boolean shown;
	boolean fullSize;
	int width;
	public int height;

	FolderModel() {
		apps = new ArrayList<>();
		shown = true;
		fullSize = true;
	}
}
