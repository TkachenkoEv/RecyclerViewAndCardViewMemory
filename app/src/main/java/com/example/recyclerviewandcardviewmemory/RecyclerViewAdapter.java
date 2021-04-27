package com.example.recyclerviewandcardviewmemory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.GameViewHolder> {

    private final ArrayList<Img> arrImg;

    private final String drawable;
    private final Context context;

    private static final int CELL_CLOSE = 0;
    private static final int CELL_OPEN = 1;
    private static final int CELL_DELETE = -1;

    public void setOnCellClickListener(RecyclerViewAdapter.onCellClickListener onCellClickListener) {
        this.onCellClickListener = onCellClickListener;
    }

    private onCellClickListener onCellClickListener;

    interface onCellClickListener {
        void onCellClick(int position);
    }

    public RecyclerViewAdapter(Context context) {

        arrImg = new ArrayList<>();

        this.context = context;

        drawable = context.getResources().getString(R.string.drawable_img);

        fillArrImg();
    }

    public void checkOpenCells() {

        ArrayList<Integer> arrIndex = new ArrayList<>();

        for (int i = 0; i < arrImg.size(); i++) {

            if (arrImg.get(i).getStatus() == CELL_OPEN) {
                arrIndex.add(i);
            }
        }

        if (arrIndex.size() == 2) {
            int first = arrIndex.get(0);
            int second = arrIndex.get(1);

            if (arrImg.get(first).getId() == arrImg.get(second).getId()) {
                arrImg.get(first).setStatus(CELL_DELETE);
                arrImg.get(second).setStatus(CELL_DELETE);
            } else {
                arrImg.get(first).setStatus(CELL_CLOSE);
                arrImg.get(second).setStatus(CELL_CLOSE);
            }
        }
    }

    public void openCell(int position) {
        if (arrImg.get(position).getStatus() != CELL_DELETE)
            arrImg.get(position).setStatus(CELL_OPEN);

        notifyDataSetChanged();
    }

    public boolean checkGameOver() {

        boolean isGameOver = true;

        for (int i = 0; i < arrImg.size(); i++) {

            if (arrImg.get(i).getStatus() == CELL_CLOSE) {
                isGameOver = false;
            }
        }

        return isGameOver;
    }

    private void fillArrImg() {

        int countColumn = Integer.parseInt(context.getResources().getString(R.string.count_column));

        for (int i = 1; i <= countColumn*countColumn/2; i++) {
            setObjectInArrImg(drawable + i);
        }

        Collections.shuffle(arrImg);
    }

    void setObjectInArrImg(String drawable) {
        int id = context.getResources().getIdentifier(drawable, "id", context.getPackageName());

        arrImg.add(new Img(id, CELL_CLOSE));
        arrImg.add(new Img(id, CELL_CLOSE));
    }


    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_layout, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {

        switch (arrImg.get(position).getStatus()) {
            case CELL_OPEN:

                holder.imageViewImage.setImageResource(arrImg.get(position).getId());
                break;
            case CELL_CLOSE:

                holder.imageViewImage.setImageResource(android.R.drawable.star_on);
                break;
            default:
                holder.imageViewImage.setImageResource(android.R.drawable.star_off);
        }

    }

    @Override
    public int getItemCount() {
        return arrImg.size();
    }

    class GameViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewImage;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewImage = itemView.findViewById(R.id.imageViewImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCellClickListener != null) {
                        onCellClickListener.onCellClick(getAdapterPosition());
                    }
                }
            });

        }
    }
}
