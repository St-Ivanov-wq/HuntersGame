package com.example.huntersgame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CreatureAdapter extends RecyclerView.Adapter<CreatureAdapter.CreatureViewHolder> {

    private List<Creature> creatures;

    public CreatureAdapter(List<Creature> creatures) {
        this.creatures = creatures;
    }

    @NonNull
    @Override
    public CreatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_creature, parent, false);
        return new CreatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreatureViewHolder holder, int position) {
        Creature creature = creatures.get(position);
        holder.tvName.setText(creature.getName() + " (" + creature.getNameBulgarian() + ")");
        holder.tvDescription.setText(creature.getDescription());
        holder.tvPoints.setText("⭐ " + creature.getPointsValue() + " points");
        holder.tvHabitat.setText("🏞️ " + creature.getHabitat());
        // holder.ivIcon.setImageResource(creature.getImageResId()); // Uncomment when icons are ready
    }

    @Override
    public int getItemCount() {
        return creatures.size();
    }

    static class CreatureViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDescription;
        TextView tvPoints;
        TextView tvHabitat;
        ImageView ivIcon;

        CreatureViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_creature_name);
            tvDescription = itemView.findViewById(R.id.tv_creature_description);
            tvPoints = itemView.findViewById(R.id.tv_creature_points);
            tvHabitat = itemView.findViewById(R.id.tv_creature_habitat);
            ivIcon = itemView.findViewById(R.id.iv_creature_icon);
        }
    }
}