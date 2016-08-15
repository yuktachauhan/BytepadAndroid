package in.silive.bytepad.Activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

import in.silive.bytepad.Adapters.PapersListAdapter;
import in.silive.bytepad.Application.BytepadApplication;
import in.silive.bytepad.Network.CheckConnectivity;
import in.silive.bytepad.PaperDatabaseModel;
import in.silive.bytepad.PaperDatabaseModel_Table;
import in.silive.bytepad.PrefManager;
import in.silive.bytepad.R;
import in.silive.bytepad.SnackBarListener;

public class MainActivity extends AppCompatActivity implements SnackBarListener, FlowContentObserver.OnModelStateChangedListener {
    public static List<PaperDatabaseModel> paperList = new ArrayList<>();
    public AutoCompleteTextView search_paper;
    public Context c;
    public RecyclerView rview;
    public TabLayout tabview;
    public TabItem all, st, put, ut, saved;
    public TabHost tabHost;
    public CoordinatorLayout coordinatorLayout;
    PapersListAdapter adapter;
    String query = "%";
    String paperType = "%";
    Toolbar toolbar;
    ImageView ivClearSearch;
    RelativeLayout recyclerEmptyView;
    FlowContentObserver observer;
    PrefManager prefManager;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BytepadApplication application = (BytepadApplication)getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        prefManager = new PrefManager(this);
        this.observer = new FlowContentObserver();
        this.observer.registerForContentChanges(this, PaperDatabaseModel.class);
        this.observer.addModelChangeListener(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        Log.d("Bytepad", "MainActivity created");
        search_paper = (AutoCompleteTextView) findViewById(R.id.search_paper);
        Log.d("Bytepad", "Search bar added");
        tabview = (TabLayout) findViewById(R.id.tabview);
        Log.d("Bytepad", "Tab Layout added");
        rview = (RecyclerView) findViewById(R.id.rview);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rview.setLayoutManager(mLayoutManager);
        query = "";
        ivClearSearch = (ImageView) findViewById(R.id.ivClearSearch);
        recyclerEmptyView = (RelativeLayout) findViewById(R.id.recyclerEmptyView);
        tabview.addTab(tabview.newTab().setText("ALL"), 0);
        tabview.addTab(tabview.newTab().setText("ST"), 1);
        tabview.addTab(tabview.newTab().setText("PUT"), 2);
        tabview.addTab(tabview.newTab().setText("UT"), 3);
        tabview.addTab(tabview.newTab().setText("SAVED"), 4);
        tabview.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int i = tab.getPosition();
                switch (i) {
                    case 0:
                        paperType = "%";
                        break;
                    case 1:
                        paperType = "ST%";
                        break;
                    case 2:
                        paperType = "PUT%";
                        break;
                    case 3:
                        paperType = "UT%";
                        break;
                    case 4:
                        paperType = "download";
                        break;

                }
                setUpList(query);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (!CheckConnectivity.isNetConnected(this)) {
            TabLayout.Tab tab = tabview.getTabAt(4);
            tab.select();
        }
        search_paper.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                query = editable.toString();
                setUpList(query);
            }
        });

        ivClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_paper.setText("");
            }
        });
        setUpList(query);

    }

    @Override
    protected void onStop() {
        observer.unregisterForContentChanges(this);
        super.onStop();
    }

    public void setUpList(String query) {
        SQLCondition secondCondition;
        if (paperType.equalsIgnoreCase("download"))
            secondCondition = PaperDatabaseModel_Table.downloaded.is(true);
        else
            secondCondition = PaperDatabaseModel_Table.ExamCategory.like(paperType);
        paperList = new Select().from(PaperDatabaseModel.class)
                .where(PaperDatabaseModel_Table.Title.like("%" + query + "%"), secondCondition)
                .queryList();
        adapter = new PapersListAdapter(this, paperList);
        rview.setAdapter(adapter);
        if (paperList.size() != 0) {
            recyclerEmptyView.setVisibility(View.GONE);
        } else {
            recyclerEmptyView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public CoordinatorLayout getCoordinatorLayout() {
        return this.coordinatorLayout;
    }

    @Override
    public void onModelStateChanged(@Nullable Class<? extends Model> table, BaseModel.Action action, @NonNull SQLCondition[] primaryKeyValues) {
        if (action.equals(BaseModel.Action.UPDATE)) {
            for (SQLCondition cond : primaryKeyValues) {
                if (cond.columnName().contains("id")) {
                    updateModelView(Integer.parseInt(cond.value().toString()));
                }
            }
        }
    }


    private void updateModelView(int id) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (id == adapter.getPapersList().get(i).id) {
                final int finalI = i;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemChanged(finalI);
                    }
                });
                break;
            }
        }
    }
}