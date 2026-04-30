
// dont need

package com.example.huntersgame;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CollectionActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private Button btnBack;
    private TextView tvEmptyCollection;
    private List<Creature> capturedCreatures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        recyclerView = findViewById(R.id.recycler_view);
        btnBack = findViewById(R.id.btn_back);
        tvEmptyCollection = findViewById(R.id.tv_empty_collection);

        // Get all creatures
        List<Creature> allCreatures = CreatureData.getCreatures();
        capturedCreatures = new ArrayList<>();

        // Filter only captured creatures
        for (Creature creature : allCreatures) {
            if (creature.isCaptured()) {
                capturedCreatures.add(creature);
            }
        }

        // Setup recycler view
        if (capturedCreatures.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyCollection.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyCollection.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            // We'll create an adapter later
            recyclerView.setAdapter(new CreatureAdapter(capturedCreatures));
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}