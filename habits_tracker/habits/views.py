from django.shortcuts import render
from django.views.generic import ListView, DetailView, CreateView, UpdateView, DeleteView, View
from django.contrib.auth.mixins import LoginRequiredMixin
from .models import Habit
from django.urls import reverse_lazy
import csv
from django.http import HttpResponse

import datetime
from django.utils import timezone

class HabitListView(LoginRequiredMixin, ListView):
    model = Habit
    template_name = 'habits/habit_list.html'

    def get_queryset(self):
        return Habit.objects.filter(user=self.request.user)

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        today = timezone.now().date()
        days = [today - datetime.timedelta(days=i) for i in range(5)]
        context['days'] = days

        habits_with_reps = []
        for habit in context['object_list']:
            reps = {rep.date: rep for rep in habit.repetition_set.filter(date__in=days)}
            habits_with_reps.append((habit, reps))

        context['habits_with_reps'] = habits_with_reps
        return context

import calendar

class HabitDetailView(LoginRequiredMixin, DetailView):
    model = Habit
    template_name = 'habits/habit_detail.html'

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        today = timezone.now().date()
        cal = calendar.Calendar()
        month_days = cal.monthdatescalendar(today.year, today.month)

        reps = {rep.date for rep in self.object.repetition_set.filter(date__year=today.year, date__month=today.month)}

        context['month_days'] = month_days
        context['reps'] = reps
        context['current_month'] = today.month
        return context

class HabitCreateView(LoginRequiredMixin, CreateView):
    model = Habit
    fields = ['name', 'description']
    template_name = 'habits/habit_form.html'
    success_url = reverse_lazy('habits:habit_list')

    def form_valid(self, form):
        form.instance.user = self.request.user
        return super().form_valid(form)

class HabitUpdateView(LoginRequiredMixin, UpdateView):
    model = Habit
    fields = ['name', 'description']
    template_name = 'habits/habit_form.html'
    success_url = reverse_lazy('habits:habit_list')

class HabitDeleteView(LoginRequiredMixin, DeleteView):
    model = Habit
    template_name = 'habits/habit_confirm_delete.html'
    success_url = reverse_lazy('habits:habit_list')

from django.shortcuts import get_object_or_404
from .models import Repetition

class ExportCSVView(LoginRequiredMixin, View):
    def get(self, request, *args, **kwargs):
        response = HttpResponse(content_type='text/csv')
        response['Content-Disposition'] = 'attachment; filename="habits.csv"'

        writer = csv.writer(response)
        writer.writerow(['Habit', 'Date', 'Value'])

        habits = Habit.objects.filter(user=request.user)
        for habit in habits:
            for repetition in habit.repetition_set.all():
                writer.writerow([habit.name, repetition.date, repetition.value])

        return response

class LogRepetitionView(LoginRequiredMixin, View):
    def post(self, request, *args, **kwargs):
        habit_id = request.POST.get('habit_id')
        date_str = request.POST.get('date')
        date = datetime.datetime.strptime(date_str, '%Y-%m-%d').date()
        habit = get_object_or_404(Habit, pk=habit_id, user=request.user)

        rep, created = Repetition.objects.get_or_create(habit=habit, date=date)
        if not created:
            rep.delete()

        return HttpResponse(status=204)
