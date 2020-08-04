package uz.mnsh.sayyidsafo.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import uz.mnsh.sayyidsafo.R
import uz.mnsh.sayyidsafo.ui.activity.MainActivity.Companion.playerAdapter

class CarModeFragment : Fragment(R.layout.fragment_car_mode) {

    private lateinit var tvTitle: AppCompatTextView
    private lateinit var imgPlay: AppCompatImageView
    private lateinit var imgReplayBack: AppCompatImageView
    private lateinit var imgReplayNext: AppCompatImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitle = view.findViewById(R.id.tv_title)
        imgPlay = view.findViewById(R.id.img_play)
        imgReplayBack = view.findViewById(R.id.img_replay_back)
        imgReplayNext = view.findViewById(R.id.img_replay_next)
    }

    override fun onResume() {
        super.onResume()
        bindUI()
    }

    private fun bindUI() {
        if (playerAdapter != null) {
            playerAdapter!!.getCurrentTitle().observe(viewLifecycleOwner, Observer {
                if (it == null) return@Observer
                tvTitle.text = it
            })
            if (playerAdapter!!.getMediaPlayer()!!.isPlaying) {
                imgPlay.setImageResource(R.drawable.stop)
            }
        }

        imgPlay.setOnClickListener {
            if (playerAdapter != null) {
                if (playerAdapter!!.getMediaPlayer()!!.isPlaying) {
                    imgPlay.setImageResource(R.drawable.play)
                } else {
                    imgPlay.setImageResource(R.drawable.stop)
                }
                playerAdapter!!.resumeOrPause()
            }
        }

        imgReplayBack.setOnClickListener {
            if (playerAdapter != null) {
                if (playerAdapter!!.getMediaPlayer()!!.currentPosition.minus(30000) > 0) {
                    playerAdapter!!.seekTo(
                        playerAdapter!!.getMediaPlayer()!!.currentPosition.minus(
                            30000
                        )
                    )
                } else {
                    playerAdapter!!.seekTo(0)
                }
            }
        }

        imgReplayNext.setOnClickListener {
            if (playerAdapter != null) {
                if (playerAdapter!!.getMediaPlayer()!!.currentPosition.plus(30000) < playerAdapter!!.getMediaPlayer()!!.duration) {
                    playerAdapter!!.seekTo(playerAdapter!!.getMediaPlayer()!!.currentPosition.plus(30000))
                }else{
                    playerAdapter!!.skip(true)
                }
            }
        }

    }
}