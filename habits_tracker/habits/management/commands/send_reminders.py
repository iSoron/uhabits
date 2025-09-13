from django.core.management.base import BaseCommand
from django.core.mail import send_mail
from django.utils import timezone
from habits.models import Habit

class Command(BaseCommand):
    help = 'Sends habit reminders to users'

    def handle(self, *args, **options):
        now = timezone.now()
        habits_to_remind = Habit.objects.filter(
            send_reminder=True,
            reminder_time__hour=now.hour,
            reminder_time__minute=now.minute
        )

        for habit in habits_to_remind:
            send_mail(
                f'Reminder: {habit.name}',
                f'This is a reminder to complete your habit: {habit.name}',
                'from@example.com',
                [habit.user.email],
                fail_silently=False,
            )
            self.stdout.write(self.style.SUCCESS(f'Successfully sent reminder for "{habit.name}" to {habit.user.email}'))
