package com.example.runningmate.domain.use_case

import com.example.runningmate.data.source.LocationDataSource
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocationUseCase @Inject constructor(
    private val locationDataSource: LocationDataSource
) {
    val locationFlow: Flow<LatLng> = locationDataSource.locationFlow
    val pathPointsFlow: Flow<List<LatLng>> = locationDataSource.pathPointsFlow

    operator fun invoke(): Flow<LatLng> {
        return locationDataSource.getLocationUpdates()
    }
}
