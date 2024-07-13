package com.chinmay.diat;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private Context context;
    private List<DepartmentModel> departmentModelList;
    private List<DepartmentModel> filteredList;

    public DepartmentAdapter(Context context, List<DepartmentModel> departmentModelList) {
        this.context = context;
        this.departmentModelList = departmentModelList;
        this.filteredList = new ArrayList<>(departmentModelList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_department, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DepartmentModel departmentModel = filteredList.get(position);

        holder.departmentName.setText(departmentModel.getName());
        holder.departmentIcon.setImageResource(departmentModel.getIconResId());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event, start appropriate departmentModel activity
                Intent intent = null;
                switch (departmentModel.getName()) {
                    case "Administration":
                        intent = new Intent(context, Administration.class);
                        break;

                    case "Academics":
                        intent = new Intent(context, Academics.class);
                        break;

                    case "Research":
                        intent = new Intent(context, Research.class);
                        break;

                    case "Faculty":
                        intent = new Intent(context, Faculty.class);
                        break;

                    case "Students":
                        intent = new Intent(context, Students.class);
                        break;

                    case "Non DIAT Students":
                        intent = new Intent(context, NonDiatStudents.class);
                        break;

                    case "Reimbursement":
                        intent = new Intent(context, Reimbursement.class);
                        break;


                    // Add cases for other departments as needed
                }
                if (intent != null) {
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView departmentIcon;
        TextView departmentName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            departmentIcon = itemView.findViewById(R.id.imageViewDepartmentIcon);
            departmentName = itemView.findViewById(R.id.textViewDepartmentName);
        }
    }

    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(departmentModelList);
        } else {
            text = text.toLowerCase().trim();
            for (DepartmentModel departmentModel : departmentModelList) {
                if (departmentModel.getName().toLowerCase().contains(text)) {
                    filteredList.add(departmentModel);
                }
            }
        }
        notifyDataSetChanged();
    }
}

