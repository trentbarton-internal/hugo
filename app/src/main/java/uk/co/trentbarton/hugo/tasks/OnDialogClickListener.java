package uk.co.trentbarton.hugo.tasks;

import uk.co.trentbarton.hugo.dialogs.CustomEditTextDialog;

public interface OnDialogClickListener {

    /**
     * This method should return a boolean to indicate wether or not to close the dialog {@link CustomEditTextDialog}
     * returning true will clsoe the dialog while a false value will keep the dialog open for you to clsoe manually.
     **/
    boolean onClick();

}
