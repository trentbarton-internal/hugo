package uk.co.trentbarton.hugo.interfaces;

import uk.co.trentbarton.hugo.customview.RadioItem;

public interface RadioItemClickedListener {
    void onItemClicked(int position, RadioItem item, boolean isChecked);
}
