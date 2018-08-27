package co.aulatech.oneshapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static String DEFAULT_IMAGE = "\uD83D\uDECD";
    public static String DRESS = "\uD83D\uDC57";
    public static String FOOD = "\uD83C\uDF72";
    public static String ELECTRONICS = "\uD83D\uDCF1";
    public static String SOVENIER = "\uD83C\uDFA8";

    List<Item> it;
    DBHelper dbHelper;
    DatabaseReference database;
    FirebaseUser user;
    TextView username;
    EditText name, number, location;
    public static String img;
    public static String user_name;
    public static String NAME_FROM_FIREBASE = "";
    public static String EMAIL_FROM_FIREBASE = "";
    public static String NUMBER_FROM_FIREBASE = "";
    public static String LOCATION_FROM_FIREBASE = "";
    public static String KEY_FROM_FIREBASE = "";
    public static String AUTH_USER_ID = "";
    public static Uri LINK = null;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    static boolean firebase_persistence_enable = false;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AUTHENTICATION
        ////////////////////////////////////////////////////////////////
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    AUTH_USER_ID = user.getUid();

                    // UPDATE DB TABLE
                    // -----------------------------------------------
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    dbHelper.insert(AUTH_USER_ID);
                }
            }
        };

        // FIREBASE CACHE
        ////////////////////////////////////////////////////////////////
        if (!firebase_persistence_enable) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            firebase_persistence_enable = true;
        }

        // INIT
        ///////////////////////////////////////////////////////////////////
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        number = findViewById(R.id.number);
        location = findViewById(R.id.location);


        // FireBase Dynamic Link
        ////////////////////////////////////////////////////////////////
        //        FirebaseDynamicLinks.getInstance().createDynamicLink()
        //                .setLink(Uri.parse("http://www.budgeat.co"))
        //                .setDynamicLinkDomain("oneshapp.page.link/H3Ed")
        //                .setSocialMetaTagParameters(
        //                        new DynamicLink.SocialMetaTagParameters.Builder()
        //                                .setTitle("Example of a Dynamic Link")
        //                                .setDescription("This link works whether the app is installed or not!")
        //                                .build())
        //                .buildShortDynamicLink()
        //                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
        //                    @Override
        //                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
        //
        //                        if (task.isSuccessful()) {
        //                            LINK = task.getResult().getShortLink();
        //                        } else {
        //                            // Error
        //                            // ...
        //                        }
        //                    }
        //                });

        // GET DATA FROM FIREBASE
        ////////////////////////////////////////////////////////////////
        database = FirebaseDatabase.getInstance().getReference();
        database.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    assert user.getEmail() != null;
                    String fkey = child.getKey();
                    String fname = (String) child.child("name").getValue();
                    String femail = (String) child.child("email").getValue();
                    String fnumber = (String) child.child("number").getValue();
                    String flocation = (String) child.child("location").getValue();

                    if (AUTH_USER_ID.equals(getUserFromDB()) && user.getEmail().equals(femail)) {
                        NAME_FROM_FIREBASE = fname;
                        EMAIL_FROM_FIREBASE = femail;
                        NUMBER_FROM_FIREBASE = fnumber;
                        LOCATION_FROM_FIREBASE = flocation;
                        KEY_FROM_FIREBASE = fkey;
                    }
                }

                name.setText(NAME_FROM_FIREBASE);
                username.setText(NAME_FROM_FIREBASE + "'s Store");
                number.setText(NUMBER_FROM_FIREBASE);
                location.setText(LOCATION_FROM_FIREBASE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        it = new ArrayList<>();
        final RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        final ItemAdapter adapter = new ItemAdapter(it, getApplicationContext());
        database.child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    it.add(new Item("", "$0.00", "No items yet: Post an item to share to your Social Media", DEFAULT_IMAGE, "",LINK, NUMBER_FROM_FIREBASE));
                }
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String item_with_auth_id = (String) child.child("user_id").getValue();
                        assert item_with_auth_id != null;
                    if (item_with_auth_id.equals(AUTH_USER_ID) && child.exists()) {
                            it.add(new Item(
                                    child.getKey(),
                                    "$" + child.child("price").getValue() + ".00",
                                    (String) child.child("desc").getValue(),
                                    (String) child.child("image").getValue(),
                                    (String)  child.child("in_stock").getValue(),
                                    LINK,
                                    NUMBER_FROM_FIREBASE
                            ));
                        }
                    adapter.notifyDataSetChanged();
                }

                rv.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                rv.setLayoutManager(llm);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                rv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // GET IMAGE FROM FIREBASE STORAGE
        ////////////////////////////////////////////////////////////////
        final ImageView iv = (ImageView) findViewById(R.id.imageView);
        database.child("shop_image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Glide.with(getApplicationContext())
                        .load(dataSnapshot.child("image").getValue())
                        .into(iv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // UPDATE DATA FROM FIREBASE
        ///////////////////////////////////////////////////////////////////
        final Map<String, String> data = new HashMap<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference new_user_record = database.getReference("users");

        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                data.put("name", name.getText().toString());
                data.put("number", NUMBER_FROM_FIREBASE);
                data.put("location", LOCATION_FROM_FIREBASE);
                data.put("email", EMAIL_FROM_FIREBASE);
                data.put("user_id", getUserFromDB());
                new_user_record.child(KEY_FROM_FIREBASE).setValue(data);
                finish();
                startActivity(getIntent());
                return true;
            }
        });
        number.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                data.put("name", NAME_FROM_FIREBASE);
                data.put("number", number.getText().toString());
                data.put("location", LOCATION_FROM_FIREBASE);
                data.put("email", EMAIL_FROM_FIREBASE);
                data.put("user_id", getUserFromDB());
                new_user_record.child(KEY_FROM_FIREBASE).setValue(data);
                finish();
                startActivity(getIntent());
                return true;
            }
        });
        location.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                data.put("name", NAME_FROM_FIREBASE);
                data.put("number", NUMBER_FROM_FIREBASE);
                data.put("location", location.getText().toString());
                data.put("email", EMAIL_FROM_FIREBASE);
                data.put("user_id", getUserFromDB());
                new_user_record.child(KEY_FROM_FIREBASE).setValue(data);
                finish();
                startActivity(getIntent());
                return true;
            }
        });


        // Order count
        ///////////////////////////////////////////////////////////////////
        TextView o = findViewById(R.id.orders);
        o.setText("12");
    }

    /**********************************************************************************
     * RETRIEVE USERNAME FROM INTERNAL DB
     *********************************************************************************/
    public String getUserFromDB() {
        dbHelper = new DBHelper(this);
        final Cursor cursor = dbHelper.getRecord(1);
        if (cursor.moveToFirst()) {
            user_name = cursor.getString(1);
            cursor.close();
        }
        return user_name;
    }

    /**********************************************************************************
     * SIGN OUT LOGIC
     *********************************************************************************/
    public void signOut() {
        auth.signOut();
        finish();
        Toast.makeText(getApplicationContext(), "You're now logged out", Toast.LENGTH_LONG).show();
    }

    /**********************************************************************************
     * TOP NAVIGATION INIT & LOGIC
     *********************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.add_item:
                // do action
                //////////////////////
                add_new_item();
                return true;

            case R.id.refresh:
                // do action
                //////////////////////
                finish();
                startActivity(getIntent());
                return true;

            case R.id.log_out:
                // do action
                //////////////////////
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**********************************************************************************
     * NECESSARY FOR AUTHENTICATION SESSIONS
     *********************************************************************************/
    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    /**********************************************************************************
     * ADD ITEM (DIALOG)
     *********************************************************************************/
    public void add_new_item() {
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        final View promptsView = li.inflate(R.layout.module_add_item, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set module_redeem.xmlrtdialog builder
        alertDialogBuilder.setView(promptsView);

        // set a title
        View header = li.inflate(R.layout.module_header_item, null);
        alertDialogBuilder.setCustomTitle(header);

        // Layout INIT
        ////////////////////////////////////////////////////////////////
        SeekBar seekBar_img = (SeekBar) promptsView.findViewById(R.id.seekBar);
        final TextView add_img = (TextView) promptsView.findViewById(R.id.add_item_img);
        final TextView add_price = (TextView) promptsView.findViewById(R.id.add_price);
        final TextView add_desc = (TextView) promptsView.findViewById(R.id.add_desc);


        // SEEKBAR TXT LOGIC
        // ----------------------------------------------
        add_img.setText(DEFAULT_IMAGE); // DEFAULT IMAGE
        img = DEFAULT_IMAGE;

        // SEEKBAR BTN LOGIC
        // ----------------------------------------------
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


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("ADD NEW ITEM",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                FirebaseDatabase database_post = FirebaseDatabase.getInstance();
                                DatabaseReference new_item = database_post.getReference("items");


                                if (TextUtils.isEmpty(add_price.getText().toString())) {
                                    Toast.makeText(promptsView.getContext(), "* Please enter a Price", Toast.LENGTH_SHORT).show();
                                } else if (TextUtils.isEmpty(add_desc.getText().toString())) {
                                    Toast.makeText(promptsView.getContext(), "* Please enter a Description", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (add_price.getText() != null || add_desc.getText() != null) {
                                        // Add details to Firebase
                                        Map<String, String> data = new HashMap<>();
                                        data.put("price", add_price.getText().toString());
                                        data.put("desc", add_desc.getText().toString());
                                        data.put("image", img);
                                        data.put("in_stock", "YES");
                                        data.put("user_id", AUTH_USER_ID);
                                        new_item.push().setValue(data);

                                        finish();
                                        startActivity(getIntent());
                                        Toast.makeText(getApplicationContext(), "\uD83D\uDE4C" + "Awesome", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialogBuilder.setCancelable(true);
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}