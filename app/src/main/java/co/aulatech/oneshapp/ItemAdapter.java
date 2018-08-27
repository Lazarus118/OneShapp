package co.aulatech.oneshapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.viewHolder> {
    private List<Item> r;
    private Context mContext;
    private static String img;
    private static String user_name;
    private static String SWITCH_LOGIC;

    ItemAdapter(List<Item> r, Context mContext) {
        this.r = r;
        this.mContext = mContext;
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        CardView cv_item;
        TextView cv_price, cv_desc, cv_img, cv_options, cv_in_stock;


        viewHolder(View itemView) {
            super(itemView);
            cv_item = (CardView) itemView.findViewById(R.id.cv_items);
            cv_price = (TextView) itemView.findViewById(R.id.cv_price_txt);
            cv_desc = (TextView) itemView.findViewById(R.id.cv_desc_txt);
            cv_img = (TextView) itemView.findViewById(R.id.cv_img);
            cv_options = (TextView) itemView.findViewById(R.id.cv_options);
            cv_in_stock = (TextView) itemView.findViewById(R.id.cv_in_stock);
        }
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_items, parent, false);
        viewHolder pvh = new viewHolder(v);
        return pvh;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);

    }


    @Override
    public void onBindViewHolder(final viewHolder viewHolder, final int i) {
        final Item i_feed = r.get(i);
        ///////////////////////////////////////////////////////////////////////////////
        viewHolder.cv_price.setText(i_feed.getPrice());
        viewHolder.cv_desc.setText(i_feed.getDesc());
        viewHolder.cv_img.setText(i_feed.getImg());
        ///////////////////////////////////////////////////////////////////////////////
        if (i_feed.getIn_stock().equals("YES")) {
            viewHolder.cv_in_stock.setVisibility(View.GONE);
        } else if (i_feed.getIn_stock().equals("NO")) {
            viewHolder.cv_in_stock.setVisibility(View.VISIBLE);
        }
        ///////////////////////////////////////////////////////////////////////////////
        viewHolder.cv_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody =
                                "#OneShapp" + "\n" + "+ -------------------------------" + "\n" +
                                "+ New Item for SALE!!!" + "\n" +
                                "+ " + i_feed.getImg() + " | " + i_feed.getPrice() + "\n" +
                                        "+ " + i_feed.getDesc() + "\n" +
                                "Message for more info:" + "\n" + "+ -------------------------------" + "\n" +
                                i_feed.getTel_num();

                PackageManager pm = mContext.getPackageManager();
                try {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share on WhatsApp");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    sharingIntent.setPackage("com.whatsapp");
                    mContext.startActivity(sharingIntent);
                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(mContext, "WhatsApp not installed?", Toast.LENGTH_LONG).show();
                }

            }
        });
        ///////////////////////////////////////////////////////////////////////////////
        viewHolder.cv_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, viewHolder.cv_options);
                //inflating menu from xml resource
                popup.inflate(R.menu.cv_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        FirebaseDatabase database_post = FirebaseDatabase.getInstance();
                        final DatabaseReference new_item = database_post.getReference("items");

                        switch (item.getItemId()) {
                            case R.id.edit:
                                // Init
                                ////////////////////////////////////////////////////////////////
                                final String DEFAULT_IMAGE = "\uD83D\uDECD";
                                final String DRESS = "\uD83D\uDC57";
                                final String FOOD = "\uD83C\uDF72";
                                final String ELECTRONICS = "\uD83D\uDCF1";
                                final String SOVENIER = "\uD83C\uDFA8";
                                LayoutInflater li = LayoutInflater.from(mContext);
                                final View promptsView = li.inflate(R.layout.module_add_item, null);
                                // set a title
                                ////////////////////////////////////////////////////////////////
                                View header = li.inflate(R.layout.module_header_item_edit, null);
                                // Layout INIT
                                ////////////////////////////////////////////////////////////////
                                SeekBar seekBar_img = (SeekBar) promptsView.findViewById(R.id.seekBar);
                                final TextView add_img = (TextView) promptsView.findViewById(R.id.add_item_img);
                                final TextView add_price = (TextView) promptsView.findViewById(R.id.add_price);
                                final TextView add_desc = (TextView) promptsView.findViewById(R.id.add_desc);
                                final Switch stock = (Switch) promptsView.findViewById(R.id.switch1);


                                add_price.setText(i_feed.getPrice());
                                add_desc.setText(i_feed.getDesc());
                                add_img.setText(i_feed.getImg()); // DEFAULT IMAGE
                                img = DEFAULT_IMAGE;

                                // SEEKBAR BTN LOGIC
                                ////////////////////////////////////////////////////////////////
                                seekBar_img.getProgressDrawable().setColorFilter(Color.parseColor("#7f2c90"), PorterDuff.Mode.SRC_IN);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    seekBar_img.getThumb().setColorFilter(Color.parseColor("#7f2c90"), PorterDuff.Mode.SRC_IN);
                                }
                                seekBar_img.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                        if (i == 0) {
                                            add_img.setText(DEFAULT_IMAGE);
                                            img = DEFAULT_IMAGE;
                                        }
                                        if (i == 1) {
                                            add_img.setText(DRESS);
                                            img = DRESS;
                                        }
                                        if (i == 2) {
                                            add_img.setText(FOOD);
                                            img = FOOD;
                                        }
                                        if (i == 3) {
                                            add_img.setText(ELECTRONICS);
                                            img = ELECTRONICS;
                                        }
                                        if (i == 4) {
                                            add_img.setText(SOVENIER);
                                            img = SOVENIER;
                                        }
                                    }

                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {
                                    }

                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {
                                    }
                                });

                                new AlertDialog.Builder(view.getRootView().getContext())
                                        .setCustomTitle(header)
                                        .setPositiveButton("EDIT ITEM", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                if (TextUtils.isEmpty(add_price.getText().toString())) {
                                                    Toast.makeText(promptsView.getContext(), "* Please enter a Price", Toast.LENGTH_SHORT).show();
                                                } else if (TextUtils.isEmpty(add_desc.getText().toString())) {
                                                    Toast.makeText(promptsView.getContext(), "* Please enter a Description", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (add_price.getText() != null || add_desc.getText() != null) {

                                                        // IN STOCK LOGIC
                                                        ////////////////////////////////////////////////////////////////
                                                        if (stock.isChecked()) {
                                                            SWITCH_LOGIC = "YES";
                                                            i_feed.setIn_stock("YES");
                                                        } else if (!stock.isChecked()) {
                                                            SWITCH_LOGIC = "NO";
                                                            i_feed.setIn_stock("NO");
                                                        }

                                                        // Add details to Firebase
                                                        Map<String, String> data = new HashMap<>();
                                                        data.put("price", add_price.getText().toString().replace("$", "").replace(".00", ""));
                                                        data.put("desc", add_desc.getText().toString());
                                                        data.put("image", img);
                                                        data.put("in_stock", SWITCH_LOGIC);
                                                        data.put("user_id", getIDFromDB());
                                                        new_item.child(i_feed.get_id()).setValue(data);

                                                        Toast.makeText(mContext, "\uD83D\uDE4C" + "Awesome", Toast.LENGTH_LONG).show();
                                                    }
                                                }

                                            }
                                        })
                                        .setView(promptsView)
                                        .show();
                                return true;

                            case R.id.delete:
                                new_item.child(i_feed.get_id()).removeValue();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return r.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    /**********************************************************************************
     * RETRIEVE USERNAME FROM INTERNAL DB
     *********************************************************************************/
    public String getIDFromDB() {
        DBHelper dbHelper = new DBHelper(mContext);
        final Cursor cursor = dbHelper.getRecord(1);
        if (cursor.moveToFirst()) {
            user_name = cursor.getString(1);
            cursor.close();
        }
        return user_name;
    }
}