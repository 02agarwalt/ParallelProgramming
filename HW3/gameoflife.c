// gameoflife.c
// Name: Tanay Agarwal
// JHED: tagarwa2

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include "mpi.h"

#define DEFAULT_ITERATIONS 64
#define GRID_WIDTH  256
#define DIM  16     // assume a square grid

// documentation: http://mpitutorial.com/tutorials/mpi-send-and-receive/
// http://mpitutorial.com/tutorials/dynamic-receiving-with-mpi-probe-and-mpi-status/

void update_global(int *, int, int);
void update_global(int * main_grid, int nrows, int num_procs) {
  for(int i = 1; i < num_procs; i++) {
    MPI_Send(main_grid, DIM * DIM, MPI_INT, i, 10, MPI_COMM_WORLD);
  }
}

int get_nbs(int *, int, int);
int get_nbs(int * sub_grid, int i, int nrows) {
  int counter = 0;
  
  if(sub_grid[i + DIM] == 1) {
      counter++;
  } 
  if(sub_grid[i + DIM - 1] == 1) {
      counter++;
  }
  if(sub_grid[i - 1] == 1) {
      counter++;
  }
  if(sub_grid[i - DIM] == 1) {
      counter++;
  }
  if(sub_grid[i - DIM + 1] == 1) {
      counter++;
  } 
  if(sub_grid[i + 1] == 1) {
      counter++;
  } 
 
  if(i % DIM == 0) {
    if(sub_grid[i + DIM + 1] == 1){
      counter++;
    }
    if(sub_grid[i - 1 + (2 * DIM)] == 1) {
      counter++;
    }
  } else if((i + 1) % DIM == 0) {
    if(sub_grid[i + 1 - (2 * DIM)] == 1) {
      counter++;
    }
    if(sub_grid[i - DIM - 1] == 1) {
      counter++;
    }
  } else {
    if(sub_grid[i + DIM + 1] == 1) {
      counter++;
    }
    if(sub_grid[i - DIM - 1] == 1) {
      counter++;
    }
  }
  
  return counter;
}


int * check(int *, int);
int * check(int * sub_grid, int nrows) {
  int nbs;
  int * new_grid = calloc(DIM * nrows, sizeof(int));
  for(int i = DIM; i < (DIM * (1 + nrows)); i++) {
    nbs = get_nbs(sub_grid, i, nrows);
    if(sub_grid[i] == 1) {
      if(nbs == 3 || nbs == 2) {
        new_grid[i - DIM] = 1;
      }
    } else {
      if(nbs == 3) {
        new_grid[i - DIM] = 1;
      }
    }
  }
  return new_grid;
}

int main ( int argc, char** argv ) {

  int main_grid[ 256 ] = 
   {0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
    1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };

  // MPI Standard variable
  int num_procs;
  int ID, j;
  int iters = 0;
  int num_iterations;

  // Setup number of iterations
  if (argc == 1) {
    num_iterations = DEFAULT_ITERATIONS;
  }
  else if (argc == 2) {
    num_iterations = atoi(argv[1]);
  }
  else {
    printf("Usage: ./gameoflife <num_iterations>\n");
    exit(1);
  }

  // Messaging variables
  MPI_Status stat;
  // TODO add other variables as necessary

  // MPI Setup
  if ( MPI_Init( &argc, &argv ) != MPI_SUCCESS )
  {
    printf ( "MPI_Init error\n" );
  }

  MPI_Comm_size ( MPI_COMM_WORLD, &num_procs ); // Set the num_procs
  MPI_Comm_rank ( MPI_COMM_WORLD, &ID );

  assert ( DIM % num_procs == 0 );

  // TODO Setup your environment as necessary
  int upper[DIM];
  int lower[DIM];
  
  int * pad_row_upper;
  int * pad_row_lower;
  
  int start = (DIM / num_procs) * ID;
  int end = (DIM / num_procs) - 1 + start;
  int nrows = end + 1 - start;
  int next = (ID + 1) % num_procs;
  //int prev;
  //if (ID == 0) {
  //  int prev = num_procs - 1;
  //} else {
  //  int prev = ID - 1;
  //}
  
  int prev = ID != 0 ? ID - 1 : num_procs - 1;
  
  for ( iters = 0; iters < num_iterations; iters++ ) {
    // TODO: Add Code here or a function call to you MPI code
    if(ID != 0 && iters > 0) {
      MPI_Status status;
      MPI_Recv(&main_grid[0], DIM * DIM, MPI_INT, 0, 10, MPI_COMM_WORLD, &status);
    }
    memcpy(lower, &main_grid[DIM * end], sizeof(int) * DIM);
    pad_row_lower = malloc(sizeof(int) * DIM);
    
    memcpy(upper, &main_grid[DIM * start], sizeof(int) * DIM);
    pad_row_upper = malloc(sizeof(int) * DIM);
    
    if(num_procs > 1) {
      if(ID % 2 == 1) {
        MPI_Recv(pad_row_lower, DIM, MPI_INT, next, 1, MPI_COMM_WORLD, &stat);
        MPI_Recv(pad_row_upper, DIM, MPI_INT, prev, 1, MPI_COMM_WORLD, &stat);
      } else {
        MPI_Send(upper, DIM, MPI_INT, prev, 1, MPI_COMM_WORLD);
        MPI_Send(lower, DIM, MPI_INT, next, 1, MPI_COMM_WORLD);
      }  
      if(ID % 2 == 1) {
        MPI_Send(upper, DIM, MPI_INT, prev, 0, MPI_COMM_WORLD);
        MPI_Send(lower, DIM, MPI_INT, next, 0, MPI_COMM_WORLD);
      } else {
        MPI_Recv(pad_row_lower, DIM, MPI_INT, next, 0, MPI_COMM_WORLD, &stat);
        MPI_Recv(pad_row_upper, DIM, MPI_INT, prev, 0, MPI_COMM_WORLD, &stat);
      }
    } else {
      pad_row_lower = upper;
      pad_row_upper = lower;
    }
    int sub_grid[(2 * DIM) + (nrows * DIM)];
    memcpy(sub_grid, pad_row_upper, sizeof(int) * DIM); 
    memcpy(&sub_grid[DIM], &main_grid[DIM * start], sizeof(int) * DIM * nrows);    
    memcpy(&sub_grid[DIM + (DIM * nrows)], pad_row_lower, sizeof(int) * DIM);
    int * changed_subgrid = check(sub_grid, nrows);

    if(ID != 0) {
      MPI_Send(changed_subgrid, nrows * DIM, MPI_INT, 0, 11, MPI_COMM_WORLD);
    } else {
      for(int i = 0; i < nrows * DIM; i++) {
        main_grid[i] = changed_subgrid[i];
      }
      for(int k = 1; k < num_procs; k++) {
        MPI_Status status;
        MPI_Recv(&main_grid[DIM * (DIM / num_procs) * k], nrows * DIM, MPI_INT, k, 11, MPI_COMM_WORLD, &status);
      }

      update_global(main_grid, nrows, num_procs);
    }
    
    // Output the updated grid state
    if ( ID == 0 ) {
      printf ( "\nIteration %d: final grid:\n", iters );
      for ( j = 0; j < GRID_WIDTH; j++ ) {
        if ( j % DIM == 0 ) {
          printf( "\n" );
        }
        printf ( "%d  ", main_grid[j] );
      }
      printf( "\n" );
    }
  }

  // TODO: Clean up memory
  if(num_procs >= 2) {
    free(pad_row_upper);
    free(pad_row_lower);
  }
  MPI_Finalize(); // finalize so I can exit
}

