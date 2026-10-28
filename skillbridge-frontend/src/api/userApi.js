import { makeResourceApi } from './axios.js'
import * as profileApi from './profileApi.js'
import * as skillApi from './skillApi.js'
import * as reviewApi from './reviewApi.js'
import { educationApi } from './educationApi.js'
import { experienceApi } from './experienceApi.js'
import { certificationApi } from './certificationApi.js'
import { portfolioApi } from './portfolioApi.js'

void makeResourceApi

/**
 * The backend has no "list users" endpoint, so a person is loaded by id from the public per-user endpoints.
 * Each part is independent: a 404 profile must not hide the skills.
 */
export async function getPublicBundle(userId) {
  const [profile, skills, education, experience, certifications, portfolio, rating] = await Promise.allSettled([
    profileApi.getUserProfile(userId),
    skillApi.ofUser(userId),
    educationApi.list(userId),
    experienceApi.list(userId),
    certificationApi.list(userId),
    portfolioApi.list(userId),
    reviewApi.rating(userId),
  ])
  const v = (r, d) => (r.status === 'fulfilled' ? r.value : d)
  return {
    profile: v(profile, null),
    skills: v(skills, []),
    education: v(education, []),
    experience: v(experience, []),
    certifications: v(certifications, []),
    portfolio: v(portfolio, []),
    rating: v(rating, null),
  }
}
