package fr.ul.virtumodle.modele;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.ul.virtumodle.R;

public class AccesAdapter extends RecyclerView.Adapter<AccesAdapter.AccesViewHolder> {

    private final List<Acces> accesList;
    private final OnAccesClickListener onModifier;
    private final OnAccesClickListener onSupprimer;

    public interface OnAccesClickListener {
        void onClick(Acces acces);
    }

    public AccesAdapter(List<Acces> accesList,
                        OnAccesClickListener onModifier,
                        OnAccesClickListener onSupprimer) {
        this.accesList = accesList;
        this.onModifier = onModifier;
        this.onSupprimer = onSupprimer;
    }

    @NonNull
    @Override
    public AccesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_access, parent, false);
        return new AccesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccesViewHolder holder, int position) {
        Acces acces = accesList.get(position);
        holder.tvNom.setText(acces.getNom());

        // Clic sur l'icône de corbeille pour supprimer
        holder.btnSupprimer.setOnClickListener(v -> onSupprimer.onClick(acces));
    }

    @Override
    public int getItemCount() {
        return accesList.size();
    }

    static class AccesViewHolder extends RecyclerView.ViewHolder {
        TextView tvNom;
        ImageView btnSupprimer; // Changé de Button à ImageView

        AccesViewHolder(View itemView) {
            super(itemView);
            tvNom = itemView.findViewById(R.id.tvNom);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimer); // Plus de btnModifier
        }
    }
}