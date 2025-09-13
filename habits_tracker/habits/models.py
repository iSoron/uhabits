from django.db import models
from django.contrib.auth.models import User

class Habit(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    name = models.CharField(max_length=255)
    description = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    send_reminder = models.BooleanField(default=False)
    reminder_time = models.TimeField(null=True, blank=True)

    def __str__(self):
        return self.name

class Repetition(models.Model):
    habit = models.ForeignKey(Habit, on_delete=models.CASCADE)
    date = models.DateField()
    value = models.IntegerField(default=1)

    def __str__(self):
        return f"{self.habit.name} - {self.date}"
