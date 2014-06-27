package info.paveway.lowest;

import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public abstract class AbstractBaseDialogFragment extends DialogFragment {

    protected void toast(int id) {
        toast(getActivity().getResources().getString(id));
    }

    protected void toast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }
}
