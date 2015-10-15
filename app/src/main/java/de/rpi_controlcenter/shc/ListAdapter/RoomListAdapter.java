package de.rpi_controlcenter.shc.ListAdapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.rpi_controlcenter.shc.Data.Room;

/**
 * Listen Adapter für die Raum Liste
 */
public class RoomListAdapter extends ArrayAdapter {

    private Context context;

    /**
     * Holder for the list items.
     */
    private class ViewHolder {

        TextView titleText;
    }

    public RoomListAdapter(Context context, List<Room> items) {
        super(context, android.R.layout.simple_list_item_1, items);

        this.context = context;
    }

    /**
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Room item = (Room)getItem(position);
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {

            viewToUse = mInflater.inflate(android.R.layout.simple_list_item_1, null);

            holder = new ViewHolder();
            holder.titleText = (TextView)viewToUse.findViewById(android.R.id.text1);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        holder.titleText.setText(item.getName());
        return viewToUse;
    }
}
