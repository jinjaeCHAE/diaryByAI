package com.example.icandoit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.icandoit.R;
import com.example.icandoit.model.drawing;

import java.util.List;



public class DrawingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<drawing> drawings;

    public DrawingAdapter(List<drawing> imagesList) {
        this.drawings = imagesList;
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;

        private ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.drawView2);
            textView = itemView.findViewById(R.id.word);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawing, parent, false);
        final ImageViewHolder holder = new ImageViewHolder(view);
        final View shape = holder.imageView;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final drawing item = drawings.get(holder.getAdapterPosition());
                final DragData state = new DragData(item, shape.getWidth(), shape.getHeight());
                final View.DragShadowBuilder shadow = new View.DragShadowBuilder(shape);
                ViewCompat.startDragAndDrop(shape, null, shadow, state, 0);
                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
        drawing list = drawings.get(position);
        imageViewHolder.imageView.setImageBitmap(list.getVehicleimage());
        imageViewHolder.textView.setText(list.getVehiclename());

    }

    @Override
    public int getItemCount() {
        return drawings.size();
    }
}
//public class DrawingAdapter extends RecyclerView.Adapter<DrawingAdapter.MyViewHolder> {
//
//
//    private Context context;
//    private List<drawing> recyclerViewModels;
//
//    public DrawingAdapter(Context context, List<drawing> recyclerViewModels){
//        this.context = context;
//        this.recyclerViewModels = recyclerViewModels;
//    }
//    @NonNull
//    @Override
//    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        final View v = LayoutInflater.from(context).inflate(R.layout.drawing,parent,false);
//        final RecyclerView.ViewHolder holder= new MyViewHolder(v);
//        final View shape = holder.itemView;
//        //시바
//        holder.itemView.setOnLongClickListener(vv -> {
//            final drawing item = recyclerViewModels.get(holder.getAdapterPosition());
//            final DragData state = new DragData(item, shape.getWidth(), shape.getHeight());
//            final View.DragShadowBuilder shadow = new View.DragShadowBuilder(shape);
//            ViewCompat.startDragAndDrop(shape, null, shadow, state, 0);
//            return true;
//        });
//        return (MyViewHolder) holder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        drawing recyclerViewModel = recyclerViewModels.get(position);
//        holder.name.setText(recyclerViewModel.getVehiclename());
//        holder.image.setImageResource(recyclerViewModel.getVehicleimage());
//    }
//
//    @Override
//    public int getItemCount() {
//        return recyclerViewModels.size();
//    }
//
//    public class MyViewHolder extends RecyclerView.ViewHolder {
//        TextView name;
//        ImageView image;
//
//        public MyViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            name = itemView.findViewById(R.id.Vehiclename);
//            image = itemView.findViewById(R.id.vehicleicon);
//        }
//    }
//
//}