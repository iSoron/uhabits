from django.test import TestCase
from django.contrib.auth.models import User
from .models import Habit, Repetition
from django.urls import reverse

class HabitTestCase(TestCase):
    def setUp(self):
        self.user = User.objects.create_user(username='testuser', password='password')
        self.client.login(username='testuser', password='password')
        self.habit = Habit.objects.create(user=self.user, name='Test Habit')
        self.repetition = Repetition.objects.create(habit=self.habit, date='2025-01-01')

    def test_habit_list_view(self):
        response = self.client.get(reverse('habits:habit_list'))
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'Test Habit')

    def test_habit_detail_view(self):
        response = self.client.get(reverse('habits:habit_detail', args=[self.habit.pk]))
        self.assertEqual(response.status_code, 200)
        self.assertContains(response, 'Test Habit')

    def test_habit_create_view(self):
        response = self.client.post(reverse('habits:habit_create'), {'name': 'New Habit'})
        self.assertEqual(response.status_code, 302)
        self.assertTrue(Habit.objects.filter(name='New Habit').exists())

    def test_habit_update_view(self):
        response = self.client.post(reverse('habits:habit_update', args=[self.habit.pk]), {'name': 'Updated Habit'})
        self.assertEqual(response.status_code, 302)
        self.habit.refresh_from_db()
        self.assertEqual(self.habit.name, 'Updated Habit')

    def test_habit_delete_view(self):
        response = self.client.post(reverse('habits:habit_delete', args=[self.habit.pk]))
        self.assertEqual(response.status_code, 302)
        self.assertFalse(Habit.objects.filter(pk=self.habit.pk).exists())

    def test_export_csv_view(self):
        response = self.client.get(reverse('habits:export_csv'))
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response['Content-Type'], 'text/csv')
        self.assertIn('Test Habit,2025-01-01,1', response.content.decode())
