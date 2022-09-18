package com.example.smartvibrator.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartvibrator.MainActivity;
import com.example.smartvibrator.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment
{

	private FragmentDashboardBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

		binding = FragmentDashboardBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		return root;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		binding = null;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		((MainActivity) getActivity()).update_pattern_information();
	}
}