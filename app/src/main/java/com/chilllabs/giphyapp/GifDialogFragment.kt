package com.chilllabs.giphyapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class GifDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_GIF_URL = "gif_url"
        private const val ARG_GIF_TITLE = "gif_title"

        fun newInstance(gifUrl: String, gifTitle: String): GifDialogFragment {
            return GifDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_GIF_URL, gifUrl)
                    putString(ARG_GIF_TITLE, gifTitle)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gif_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gifImageView = view.findViewById<ImageView>(R.id.dialogGifImageView)
        val titleTextView = view.findViewById<TextView>(R.id.dialogGifTitle)

        val gifUrl = arguments?.getString(ARG_GIF_URL)
        val gifTitle = arguments?.getString(ARG_GIF_TITLE) ?: "Unnamed GIF"

        // Загружаем GIF
        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .error(R.drawable.error_placeholder)
            .into(gifImageView)

        // Устанавливаем название
        titleTextView.text = gifTitle

        // Анимация увеличения
        gifImageView.scaleX = 0.5f
        gifImageView.scaleY = 0.5f
        gifImageView.alpha = 0f
        val scaleX = ObjectAnimator.ofFloat(gifImageView, View.SCALE_X, 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(gifImageView, View.SCALE_Y, 0.5f, 1f)
        val alpha = ObjectAnimator.ofFloat(gifImageView, View.ALPHA, 0f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 300
            start()
        }

        // Закрытие при клике на фон
        view.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}