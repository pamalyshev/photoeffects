package pamalyshev.photoeffects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by pamalyshev on 02.12.15.
 */
public class EnumAdapter<T extends Enum<T> & NamedItem> extends BaseAdapter {
    private Class<T> tClass;
    private LayoutInflater inflater;
    private int layoutId;

    public EnumAdapter(Class<T> tClass, Context context, int layoutId) {
        this.tClass = tClass;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return tClass.getEnumConstants().length;
    }

    @Override
    public T getItem(int position) {
        return tClass.getEnumConstants()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) inflater.inflate(layoutId, parent, false);
        }
        view.setText(getItem(position).getNameResId());
        return view;
    }
}
