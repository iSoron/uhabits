from django.urls import path
from . import views

app_name = 'habits'

urlpatterns = [
    path('', views.HabitListView.as_view(), name='habit_list'),
    path('<int:pk>/', views.HabitDetailView.as_view(), name='habit_detail'),
    path('create/', views.HabitCreateView.as_view(), name='habit_create'),
    path('<int:pk>/update/', views.HabitUpdateView.as_view(), name='habit_update'),
    path('<int:pk>/delete/', views.HabitDeleteView.as_view(), name='habit_delete'),
    path('export/csv/', views.ExportCSVView.as_view(), name='export_csv'),
    path('log/', views.LogRepetitionView.as_view(), name='log_repetition'),
]
