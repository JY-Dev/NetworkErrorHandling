package com.example.networkerrohandling.domain.usecase

import com.example.networkerrohandling.data.model.NetworkResult
import com.example.networkerrohandling.data.repository.MovieRepository
import com.example.networkerrohandling.domain.model.MovieInfo
import com.example.networkerrohandling.util.map
import com.example.networkerrohandling.util.networkHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MovieInfoUseCase(private val movieRepository: MovieRepository) {
    suspend operator fun invoke() : NetworkResult<MovieInfo>{
        return networkHandling {
            withContext(Dispatchers.IO){
                delay(5000L)
            }
            movieRepository.getMovie().map { movie ->
                movieRepository.getMovieDetail().map { movieDetail ->
                    MovieInfo(movie.movieId,movie.title,movie.description,movieDetail.author,movieDetail.publisher,movieDetail.rate)
                }
            }
        }

    }
}